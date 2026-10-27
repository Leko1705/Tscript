package com.tscript.runtime.jit.comp.emit.jbc;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.jit.tracing.DeoptInfo;
import com.tscript.runtime.jit.tracing.ImageRunner;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.RangeIterator;
import com.tscript.runtime.typing.TObject;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

import static com.tscript.runtime.jit.comp.emit.jbc.JBCImageGenerator.*;

/**
 * Translates a trace graph into JVM bytecode.
 * <p>
 * Scheduling: the effect chain is emitted in order. Fixed values are evaluated at their
 * {@link Anchor}. Floating values are evaluated lazily at their first use and cached in a
 * JVM local variable, so that later uses (which are always dominated by the first one, since a
 * trace is linear) can reuse them. Values first computed on a deoptimization path are not
 * cached beyond that path.
 * <p>
 * Representation: values whose type is Integer, Real, Boolean or String are kept unboxed as
 * int, double, boolean and java.lang.String. All other values are TObjects.
 * <p>
 * Loops: phis live in JVM local variables. They are initialized before the loop header label
 * and assigned in parallel at the loop end, which jumps back to the header.
 * <p>
 * Trace trees: if a compiled side trace is attached to a guard, the side trace is emitted in
 * the failure path of the guard (by a child emitter sharing the method) instead of leaving
 * compiled code. A side trace that ends by jumping back to the header of the loop of its parent
 * re-assigns the phis of that loop and jumps back into it.
 */
public class EmitterImpl implements IRValueVisitor<Void, Void>, IREffectVisitor<Void, Void>, Opcodes {

    private enum Repr {
        INT("I", ILOAD, ISTORE, 1),
        REAL("D", DLOAD, DSTORE, 2),
        BOOL("Z", ILOAD, ISTORE, 1),
        STR("Ljava/lang/String;", ALOAD, ASTORE, 1),
        OBJ("Lcom/tscript/runtime/typing/TObject;", ALOAD, ASTORE, 1);

        final String desc;
        final int load, store, size;

        Repr(String desc, int load, int store, int size) {
            this.desc = desc;
            this.load = load;
            this.store = store;
            this.size = size;
        }
    }

    private static final String UTILS = "com/tscript/runtime/jit/comp/emit/jbc/JBCUtils";
    /** JVM methods take at most 255 argument slots */
    private static final int MAX_STUB_ARGUMENT_SLOTS = 250;
    private static final String TOBJECT = "com/tscript/runtime/typing/TObject";
    private static final String TOBJECT_DESC = "L" + TOBJECT + ";";
    private static final String THREAD_DESC = org.objectweb.asm.Type.getDescriptor(TThread.class);
    private static final String FRAME = org.objectweb.asm.Type.getInternalName(ImageFrame.class);
    private static final String FRAME_DESC = "L" + FRAME + ";";

    /**
     * State shared by all emitters contributing to one method.
     */
    private static final class MethodContext {
        final JBCImageGenerator generator;
        final MethodVisitor mv;
        final boolean stitching;
        int nextSlot = FIRST_FREE_SLOT;
        int stitchBudget = 4000;

        MethodContext(String className, boolean stitching) {
            this.generator = new JBCImageGenerator(className);
            this.mv = generator.getMethodVisitor();
            this.stitching = stitching;
        }
    }

    private final MethodContext context;
    private final MethodVisitor mv;
    private final Graph graph;
    private final Graph.TypeMap typeMap;

    /** the emitter of the graph whose guard this (side trace) graph is stitched into, null for the root graph */
    private final EmitterImpl parent;
    private final int depth;

    private Map<IRValue, Integer> slots = new HashMap<>();
    /** local variables holding boxed versions of unboxed values */
    private Map<IRValue, Integer> boxedSlots = new HashMap<>();
    private Label loopLabel = null;
    private LoopHeadNode closedLoop = null;

    /**
     * @param stitching true if compiled side traces should be emitted in place of guard failures
     */
    public EmitterImpl(String className, Graph graph, boolean stitching) {
        this.context = new MethodContext(className, stitching);
        this.mv = context.mv;
        this.graph = graph;
        this.typeMap = graph.typeMap();
        this.parent = null;
        this.depth = 0;
    }

    private EmitterImpl(EmitterImpl parent, Graph graph) {
        this.context = parent.context;
        this.mv = context.mv;
        this.graph = graph;
        this.typeMap = graph.typeMap();
        this.parent = parent;
        this.depth = parent.depth + 1;
    }

    private static void checkExit(IRControl exit) {
        if (exit == null) {
            throw new IllegalStateException("IRGraph must have an exit control");
        }
        if (!(exit instanceof Return) && !(exit instanceof LoopEndNode) && !(exit instanceof Exit)) {
            throw new UnsupportedOperationException("unsupported exit control " + exit);
        }
    }

    public byte[] emit() {
        checkExit(graph.exitControl());
        typeMap.flush();
        emitPrologue();
        emitBody();
        return context.generator.generate();
    }

    private void emitBody() {
        for (IREffect effect : graph.effects()) {
            effect.accept(this);
        }

        IRControl exit = graph.exitControl();
        if (exit instanceof Return ret) {
            load(ret.getValue(), Repr.OBJ);
            mv.visitInsn(ARETURN);
        }
        else if (exit instanceof Exit x) {
            if (x.getDeoptInfo() == null) x.setDeoptInfo(new DeoptInfo(x.getState(), false, true, false));
            emitVirtualObjects(x);
            if (!tryJumpBackToParentLoop(x)) {
                emitLeave(x, (DeoptInfo) x.getDeoptInfo());
            }
        }
        else {
            LoopEndNode end = (LoopEndNode) exit;
            List<Phi> phis = end.getTarget().getPhis();
            if (loopLabel == null) throw new IllegalStateException("loop end without loop begin");
            for (Phi phi : phis) {
                load(phi.getBackedge(), repr(phi));
            }
            for (int i = phis.size() - 1; i >= 0; i--) {
                Phi phi = phis.get(i);
                mv.visitVarInsn(repr(phi).store, slots.get(phi));
            }
            mv.visitJumpInsn(GOTO, loopLabel);
        }
    }

    /**
     * Loads all entry values (state of the interpreter frame or arguments) into local variables.
     * Entry values that were already provided (stitched side traces) are skipped.
     */
    private void emitPrologue() {
        for (IRValue value : graph.values()) {
            if (slots.containsKey(value)) continue;
            if (value instanceof Local || value instanceof StackValue || value instanceof Parameter) {
                value.accept(this);
                store(value);
            }
        }
    }


    /* ------------------------------------------------------------------ */
    /*  side trace stitching                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Emits the side trace attached to the given guard (if there is one that can be stitched).
     * Must be called in the failure path of the guard.
     * @return true if the side trace was emitted, false if compiled code has to be left
     */
    private boolean tryStitch(GuardBool guard) {
        if (!context.stitching || depth >= 4 || !guard.getState().isRoot()) return false;
        Graph side = graph.sideTrace(guard.getDeoptInfo());
        if (side == null || side == graph) return false;
        checkExit(side.exitControl());
        side.typeMap().flush();

        int size = side.effects().size();
        if (size > context.stitchBudget) return false;

        EmitterImpl child = new EmitterImpl(this, side);
        Map<IRValue, IRValue> entries = child.entryMapping(guard);
        if (entries == null) return false;

        context.stitchBudget -= size;
        if (Boolean.getBoolean("tscript.jit.debug")) {
            System.err.println("[jit] stitching side trace at ip " + guard.getState().ip() + " (depth " + (depth + 1) + ")");
        }
        for (Map.Entry<IRValue, IRValue> entry : entries.entrySet()) {
            IRValue sideValue = entry.getKey();
            load(entry.getValue(), child.repr(sideValue));
            int slot = newSlot(child.repr(sideValue));
            mv.visitVarInsn(child.repr(sideValue).store, slot);
            child.slots.put(sideValue, slot);
        }
        child.emitPrologue();
        child.emitBody();
        return true;
    }

    /**
     * Maps the entry values of this (side trace) graph to the values the guard of the parent
     * captured.
     * @return the mapping or null if the side trace can not be entered from the guard
     */
    private Map<IRValue, IRValue> entryMapping(GuardBool guard) {
        FrameState state = guard.getState();
        List<IRValue> values = guard.getStateValues();
        int[] indices = state.localIndices();
        Map<IRValue, IRValue> mapping = new LinkedHashMap<>();

        for (IRValue value : graph.values()) {
            IRValue captured = null;
            if (value instanceof Local local) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] == local.index) captured = values.get(i);
                }
                if (captured == null) {
                    // the local was not changed by the parent, it is read from the interpreter frame
                    // (only possible if there is one)
                    if (!(rootGraph().exitControl() instanceof LoopEndNode) && !(rootGraph().exitControl() instanceof Exit)) {
                        return null;
                    }
                    continue;
                }
            }
            else if (value instanceof StackValue stackValue) {
                if (stackValue.index >= state.stackSize()) return null;
                captured = values.get(indices.length + stackValue.index);
            }
            else if (value instanceof Parameter) {
                return null;
            }
            else {
                continue;
            }

            Repr expected = repr(value);
            Repr actual = parent.repr(captured);
            if (expected != actual && expected != Repr.OBJ) return null;
            mapping.put(value, captured);
        }
        return mapping;
    }

    private Graph rootGraph() {
        EmitterImpl emitter = this;
        while (emitter.parent != null) emitter = emitter.parent;
        return emitter.graph;
    }

    /**
     * A stitched side trace that ends by jumping to the header of the loop of its parent continues
     * in that loop: the phis of the loop are assigned the values of the side trace.
     * @return true if the jump was emitted
     */
    private boolean tryJumpBackToParentLoop(Exit exit) {
        if (parent == null || parent.loopLabel == null) return false;
        LoopHeadNode loop = parent.closedLoop;
        FrameState state = exit.getState();
        if (!state.isRoot() || state.ip() != loop.ip) return false;

        List<IRValue> values = exit.getStateValues();
        int[] indices = state.localIndices();

        // every value must either be carried by a phi of the parent loop or be unchanged
        List<Phi> phis = loop.getPhis();
        List<IRValue> phiValues = new ArrayList<>();
        Set<Integer> phiLocals = new HashSet<>();
        Set<Integer> phiStack = new HashSet<>();
        for (Phi phi : phis) {
            IRValue value;
            if (phi.getGlobal() != null) {
                value = null; // reloaded from memory
            }
            else if (phi.isLocal()) {
                value = null;
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] == phi.getLocalIndex()) value = values.get(i);
                }
                if (value == null) return false;
                phiLocals.add(phi.getLocalIndex());
            }
            else if (phi.isStackSlot()) {
                if (phi.getStackIndex() >= state.stackSize()) return false;
                value = values.get(indices.length + phi.getStackIndex());
                phiStack.add(phi.getStackIndex());
            }
            else {
                return false;
            }
            if (value != null) {
                Repr expected = parent.repr(phi);
                Repr actual = repr(value);
                if (expected != actual && expected != Repr.OBJ) return false;
            }
            phiValues.add(value);
        }
        for (int i = 0; i < indices.length; i++) {
            if (phiLocals.contains(indices[i])) continue;
            if (values.get(i) instanceof Local local && local.index == indices[i]) continue;
            // dead locals are null in the states at the header of the parent loop as well
            if (values.get(i) instanceof NullValue && isNullOnEntry(loop, indices[i])) continue;
            return false;
        }
        for (int i = 0; i < state.stackSize(); i++) {
            if (phiStack.contains(i)) continue;
            if (!(values.get(indices.length + i) instanceof StackValue sv && sv.index == i)) return false;
        }

        emitGlobalWriteBacks(exit);
        for (int i = 0; i < phis.size(); i++) {
            Phi phi = phis.get(i);
            if (phi.getGlobal() != null) {
                pushInt(phi.getGlobal().moduleId());
                pushInt(phi.getGlobal().index());
                callUtils("loadGlobal", "(II)" + TOBJECT_DESC);
                convertUnchecked(Repr.OBJ, parent.repr(phi));
            }
            else {
                load(phiValues.get(i), parent.repr(phi));
            }
        }
        for (int i = phis.size() - 1; i >= 0; i--) {
            Phi phi = phis.get(i);
            mv.visitVarInsn(parent.repr(phi).store, parent.slots.get(phi));
        }
        mv.visitJumpInsn(GOTO, parent.loopLabel);
        return true;
    }

    private static boolean isNullOnEntry(LoopHeadNode loop, int local) {
        FrameState entry = loop.getEntryState();
        if (entry == null || !entry.isRoot()) return false;
        int[] indices = entry.localIndices();
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == local) return loop.getEntryValues().get(i) instanceof NullValue;
        }
        return false;
    }

    /**
     * Converts a boxed value whose type was established before (e.g. a promoted global that was
     * written by a side trace and is reloaded).
     */
    private void convertUnchecked(Repr from, Repr to) {
        convert(from, to, null);
    }


    /* ------------------------------------------------------------------ */
    /*  helpers                                                           */
    /* ------------------------------------------------------------------ */

    private Repr repr(IRValue value) {
        IRType type = typeMap.getType(value);
        if (type == IntType.INSTANCE) return Repr.INT;
        if (type == RealType.INSTANCE) return Repr.REAL;
        if (type == BoolType.INSTANCE) return Repr.BOOL;
        if (type == StrType.INSTANCE) return Repr.STR;
        return Repr.OBJ;
    }

    private int newSlot(Repr repr) {
        int slot = context.nextSlot;
        context.nextSlot += repr.size;
        if (context.nextSlot > 65000) throw new IllegalStateException("too many local variables");
        return slot;
    }

    /**
     * Stores the value on top of the operand stack into a new local variable.
     */
    private void store(IRValue value) {
        Repr repr = repr(value);
        int slot = newSlot(repr);
        mv.visitVarInsn(repr.store, slot);
        slots.put(value, slot);
    }

    /**
     * Pushes the given value (in its representation) onto the operand stack.
     */
    private void load(IRValue value) {
        Integer slot = slots.get(value);
        if (slot != null) {
            mv.visitVarInsn(repr(value).load, slot);
            return;
        }

        if (value.isFixed() || value instanceof Phi) {
            throw new IllegalStateException("value is not available: " + value);
        }

        value.accept(this);

        if (!isCheap(value)) {
            Repr repr = repr(value);
            mv.visitInsn(repr.size == 2 ? DUP2 : DUP);
            int newSlot = newSlot(repr);
            mv.visitVarInsn(repr.store, newSlot);
            slots.put(value, newSlot);
        }
    }

    private boolean isCheap(IRValue value) {
        return value instanceof Literal<?>;
    }

    private void load(IRValue value, Repr target) {
        Repr repr = repr(value);
        if (target == Repr.OBJ && repr != Repr.OBJ) {
            // boxed values are cached as well (they are immutable)
            Integer boxed = boxedSlots.get(value);
            if (boxed != null) {
                mv.visitVarInsn(ALOAD, boxed);
                return;
            }
            load(value);
            convert(repr, Repr.OBJ, typeMap.getType(value));
            mv.visitInsn(DUP);
            int slot = newSlot(Repr.OBJ);
            mv.visitVarInsn(ASTORE, slot);
            boxedSlots.put(value, slot);
            return;
        }
        load(value);
        convert(repr, target, typeMap.getType(value));
    }

    private void convert(Repr from, Repr to, IRType fromType) {
        if (from == to) return;
        if (from == Repr.INT && to == Repr.REAL) {
            mv.visitInsn(I2D);
            return;
        }
        if (to == Repr.OBJ) {
            switch (from) {
                case INT -> callUtils("boxInt", "(I)" + TOBJECT_DESC);
                case REAL -> callUtils("boxReal", "(D)" + TOBJECT_DESC);
                case BOOL -> callUtils("boxBool", "(Z)" + TOBJECT_DESC);
                case STR -> callUtils("boxStr", "(Ljava/lang/String;)" + TOBJECT_DESC);
                default -> throw new AssertionError();
            }
            return;
        }
        if (from == Repr.OBJ) {
            switch (to) {
                case INT -> callUtils("unboxInt", "(" + TOBJECT_DESC + ")I");
                case REAL -> callUtils("unboxReal", "(" + TOBJECT_DESC + ")D");
                case BOOL -> callUtils("isTrue", "(" + TOBJECT_DESC + ")Z");
                case STR -> callUtils("unboxStr", "(" + TOBJECT_DESC + ")Ljava/lang/String;");
                default -> throw new AssertionError();
            }
            return;
        }
        throw new IllegalStateException("can not convert " + from + " (" + fromType + ") to " + to);
    }

    private void callUtils(String name, String desc) {
        mv.visitMethodInsn(INVOKESTATIC, UTILS, name, desc, false);
    }

    private void pushInt(int value) {
        if (value >= -1 && value <= 5) mv.visitInsn(ICONST_0 + value);
        else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) mv.visitIntInsn(BIPUSH, value);
        else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) mv.visitIntInsn(SIPUSH, value);
        else mv.visitLdcInsn(value);
    }

    /**
     * Converts an int comparison/condition jump into a boolean value on the stack.
     * @param jumpIfFalse the jump instruction taken if the result is false
     */
    private void materializeBoolean(int jumpIfFalse) {
        Label isFalse = new Label();
        Label end = new Label();
        mv.visitJumpInsn(jumpIfFalse, isFalse);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(isFalse);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(end);
    }

    private void loadObjectArray(List<IRValue> values) {
        pushInt(values.size());
        mv.visitTypeInsn(ANEWARRAY, TOBJECT);
        for (int i = 0; i < values.size(); i++) {
            mv.visitInsn(DUP);
            pushInt(i);
            load(values.get(i), Repr.OBJ);
            mv.visitInsn(AASTORE);
        }
    }

    private void loadThreadAndFrame() {
        mv.visitVarInsn(ALOAD, THREAD_SLOT);
        mv.visitVarInsn(ALOAD, FRAME_SLOT);
    }

    private void loadAsString(IRValue value) {
        load(value);
        switch (repr(value)) {
            case INT -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false);
            case REAL -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false);
            case BOOL -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Z)Ljava/lang/String;", false);
            case STR -> {}
            case OBJ -> callUtils("display", "(" + TOBJECT_DESC + ")Ljava/lang/String;");
        }
    }

    private void genericBinary(BinaryOp node, Opcode opcode) {
        mv.visitVarInsn(ALOAD, THREAD_SLOT);
        load(node.getLeft(), Repr.OBJ);
        load(node.getRight(), Repr.OBJ);
        pushInt(opcode.ordinal());
        callUtils("binary", "(" + THREAD_DESC + TOBJECT_DESC + TOBJECT_DESC + "I)" + TOBJECT_DESC);
    }

    private void genericUnary(UnaryOp node, Opcode opcode) {
        mv.visitVarInsn(ALOAD, THREAD_SLOT);
        load(node.getValue(), Repr.OBJ);
        pushInt(opcode.ordinal());
        callUtils("unary", "(" + THREAD_DESC + TOBJECT_DESC + "I)" + TOBJECT_DESC);
    }

    /**
     * Emits an arithmetic operation on int or double operands.
     */
    private void arithmetic(BinaryOp node, int intOp, int doubleOp, Opcode opcode) {
        Repr repr = repr(node);
        if (repr == Repr.INT && intOp >= 0) {
            load(node.getLeft(), Repr.INT);
            load(node.getRight(), Repr.INT);
            mv.visitInsn(intOp);
        }
        else if (repr == Repr.REAL && doubleOp >= 0) {
            load(node.getLeft(), Repr.REAL);
            load(node.getRight(), Repr.REAL);
            mv.visitInsn(doubleOp);
        }
        else if (repr == Repr.BOOL && intOp >= 0) {
            load(node.getLeft(), Repr.BOOL);
            load(node.getRight(), Repr.BOOL);
            mv.visitInsn(intOp);
        }
        else {
            genericBinary(node, opcode);
        }
    }

    private void comparison(BinaryOp node, int intJumpIfFalse, int doubleJumpIfFalse, Opcode opcode) {
        if (repr(node) != Repr.BOOL) {
            genericBinary(node, opcode);
            return;
        }
        Repr l = repr(node.getLeft()), r = repr(node.getRight());
        if (l == Repr.INT && r == Repr.INT) {
            load(node.getLeft());
            load(node.getRight());
            materializeBoolean(intJumpIfFalse);
        }
        else {
            load(node.getLeft(), Repr.REAL);
            load(node.getRight(), Repr.REAL);
            mv.visitInsn(DCMPG);
            materializeBoolean(doubleJumpIfFalse);
        }
    }


    /* ------------------------------------------------------------------ */
    /*  effects                                                           */
    /* ------------------------------------------------------------------ */

    @Override
    public Void visitStart(StartEffect node, Void unused) {
        return null;
    }

    @Override
    public Void visitAnchor(Anchor node, Void unused) {
        IRValue value = node.getValue();
        if (!value.isFixed() || slots.containsKey(value)) return null;
        value.accept(this);
        store(value);
        return null;
    }

    @Override
    public Void visitGuardBool(GuardBool node, Void unused) {
        load(node.getCondition(), Repr.BOOL);
        Label ok = new Label();
        mv.visitJumpInsn(IFNE, ok);

        Map<IRValue, Integer> saved = new HashMap<>(slots);
        Map<IRValue, Integer> savedBoxed = new HashMap<>(boxedSlots);
        Label savedLoopLabel = loopLabel;
        if (node.getDeoptInfo() == null) {
            List<IRValue> values = node.getStateValues();
            IRType[] types = new IRType[values.size()];
            for (int i = 0; i < types.length; i++) types[i] = typeMap.getType(values.get(i));
            node.setDeoptInfo(new DeoptInfo(node.getState(), node.isLoopExit(), false, node.isSideTraceable(), types));
        }
        emitVirtualObjects(node);
        emitGlobalWriteBacks(node);
        if (!tryStitch(node)) {
            emitLeave(node, (DeoptInfo) node.getDeoptInfo(), false);
        }
        slots = saved;
        boxedSlots = savedBoxed;
        loopLabel = savedLoopLabel;

        mv.visitLabel(ok);
        return null;
    }

    /**
     * Leaves compiled code: writes back promoted globals and returns a deoptimization.
     */
    private void emitLeave(StateCarrier carrier, DeoptInfo info) {
        emitLeave(carrier, info, true);
    }

    private void emitLeave(StateCarrier carrier, DeoptInfo info, boolean writeBack) {
        if (writeBack) emitGlobalWriteBacks(carrier);
        pushInt(ObjectCache.putId(info));
        List<IRValue> values = carrier.getStateValues();
        List<String> descs = new ArrayList<>(values.size());
        int argumentSlots = 1;
        for (IRValue value : values) {
            Repr repr = repr(value);
            descs.add(repr.desc);
            argumentSlots += repr.size;
        }
        if (argumentSlots <= MAX_STUB_ARGUMENT_SLOTS) {
            // the values are boxed by a shared stub method (keeps this method small)
            for (IRValue value : values) load(value);
            String desc = context.generator.deoptStub(descs);
            mv.visitMethodInsn(INVOKESTATIC, context.generator.getClassName(),
                    context.generator.deoptStubName(desc), desc, false);
        }
        else {
            loadObjectArray(values);
            callUtils("deopt", "(I[" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        }
        mv.visitInsn(ARETURN);
    }

    /**
     * Creates the objects whose allocations were removed (see scalar replacement) and that are
     * referenced by the state of the carrier. Must be called in the path leaving compiled code; the
     * objects are then available as values of their allocations.
     */
    private void emitVirtualObjects(StateCarrier carrier) {
        List<VirtualObjectState> objects = carrier.getVirtualObjects();
        if (objects.isEmpty()) return;
        for (VirtualObjectState state : objects) {
            NewObject object = state.object();
            pushInt(object.typeId);
            callUtils("newInstance", "(I)" + TOBJECT_DESC);
            store(object);
        }
        // methods of the objects (owned by them) referenced by the state or the members
        List<IRValue> referenced = new ArrayList<>(carrier.getStateValues());
        for (VirtualObjectState state : objects) referenced.addAll(carrier.getVirtualObjectValues(state));
        for (IRValue value : referenced) {
            if (value instanceof NewFunction function && !slots.containsKey(function)) {
                function.accept(this);
                store(function);
            }
        }
        for (VirtualObjectState state : objects) {
            List<IRValue> values = carrier.getVirtualObjectValues(state);
            for (int i = 0; i < values.size(); i++) {
                mv.visitVarInsn(ALOAD, THREAD_SLOT);
                load(state.object(), Repr.OBJ);
                pushInt(state.members()[i]);
                load(values.get(i), Repr.OBJ);
                callUtils("storeField", "(" + THREAD_DESC + TOBJECT_DESC + "I" + TOBJECT_DESC + ")V");
            }
        }
    }

    private void emitGlobalWriteBacks(StateCarrier carrier) {
        List<GlobalRef> writes = carrier.getGlobalWrites();
        List<IRValue> values = carrier.getGlobalWriteValues();
        for (int i = 0; i < writes.size(); i++) {
            mv.visitVarInsn(ALOAD, THREAD_SLOT);
            pushInt(writes.get(i).moduleId());
            pushInt(writes.get(i).index());
            load(values.get(i), Repr.OBJ);
            callUtils("writeGlobal", "(" + THREAD_DESC + "II" + TOBJECT_DESC + ")V");
        }
    }

    @Override
    public Void visitLoopBegin(LoopBegin node, Void unused) {
        LoopHeadNode loop = node.getLoop();
        if (!loop.isClosed()) return null;
        if (loopLabel != null) throw new IllegalStateException("only one closed loop per trace is supported");

        hoistLoopInvariants(node);

        for (Phi phi : loop.getPhis()) {
            load(phi.getEntry(), repr(phi));
            store(phi);
        }
        loopLabel = new Label();
        closedLoop = loop;
        mv.visitLabel(loopLabel);
        return null;
    }

    /**
     * Loop invariant code motion: evaluates floating values that are used in the loop body but do
     * not depend on the loop before the loop header, so that they are computed only once. Boxed
     * versions of invariant values passed to the runtime (e.g. call arguments) are created
     * before the loop as well.
     */
    private void hoistLoopInvariants(LoopBegin begin) {
        LoopHeadNode loop = begin.getLoop();

        Set<IRValue> variant = Collections.newSetFromMap(new IdentityHashMap<>());
        variant.addAll(loop.getPhis());
        List<IRNode> bodyUsers = new ArrayList<>();
        for (IREffect e = begin.next; e != null; e = e.next) {
            if (e instanceof Anchor anchor) variant.add(anchor.getValue());
            if (!(e instanceof Guard)) bodyUsers.add(e);
            else bodyUsers.add(((GuardBool) e).getCondition());
        }
        for (Phi phi : loop.getPhis()) bodyUsers.add(phi.getBackedge());

        Map<IRValue, Boolean> invariant = new IdentityHashMap<>();
        Set<IRValue> used = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<IRValue> boxedUses = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<IRValue> work = new ArrayDeque<>();
        for (IRNode user : bodyUsers) {
            List<IRValue> inputs = user instanceof IRValue v ? List.of(v) : user.getInputs();
            boolean needsBoxes = user instanceof IREffect && !(user instanceof Anchor);
            if (user instanceof Anchor anchor) {
                inputs = anchor.getValue().getInputs();
                needsBoxes = !(anchor.getValue() instanceof Pi);
            }
            for (IRValue input : inputs) {
                work.push(input);
                if (needsBoxes) boxedUses.add(input);
            }
        }
        while (!work.isEmpty()) {
            IRValue value = work.pop();
            if (!used.add(value) || variant.contains(value) || value.isFixed()) continue;
            for (IRValue input : value.getInputs()) work.push(input);
        }

        for (IRValue value : graph.values()) {
            if (!used.contains(value) || !isInvariant(value, variant, invariant)) continue;
            if (value.isFixed() || value instanceof Phi || value.canTrap() || dependsOnTrap(value)) continue;
            if (!(value instanceof Literal<?>) && !slots.containsKey(value)) {
                load(value);
                mv.visitInsn(repr(value).size == 2 ? POP2 : POP);
            }
            if (boxedUses.contains(value) && repr(value) != Repr.OBJ && !boxedSlots.containsKey(value)) {
                load(value, Repr.OBJ);
                mv.visitInsn(POP);
            }
        }
    }

    private boolean isInvariant(IRValue value, Set<IRValue> variant, Map<IRValue, Boolean> memo) {
        if (variant.contains(value)) return false;
        Boolean known = memo.get(value);
        if (known != null) return known;
        boolean result = true;
        if (!value.isFixed()) {
            for (IRValue input : value.getInputs()) {
                if (!isInvariant(input, variant, memo)) {
                    result = false;
                    break;
                }
            }
        }
        memo.put(value, result);
        return result;
    }

    private static boolean dependsOnTrap(IRValue value) {
        for (IRValue input : value.getInputs()) {
            if (!input.isFixed() && (input.canTrap() || dependsOnTrap(input))) return true;
        }
        return false;
    }

    @Override
    public Void visitStoreGlobal(StoreGlobal node, Void unused) {
        loadThreadAndFrame();
        pushInt(node.site);
        pushInt(node.global.moduleId());
        pushInt(node.global.index());
        load(node.getValue(), Repr.OBJ);
        callUtils("storeGlobal", "(" + THREAD_DESC + FRAME_DESC + "III" + TOBJECT_DESC + ")V");
        return null;
    }

    @Override
    public Void visitStoreMember(StoreMember node, Void unused) {
        switch (node.access) {
            case FIELD, INTERNAL -> {
                mv.visitVarInsn(ALOAD, THREAD_SLOT);
                load(node.getObject(), Repr.OBJ);
                pushInt(node.index);
                load(node.getValue(), Repr.OBJ);
                callUtils(node.access == MemberAccess.FIELD ? "storeField" : "storeInternal",
                        "(" + THREAD_DESC + TOBJECT_DESC + "I" + TOBJECT_DESC + ")V");
            }
            case EXTERNAL, SUPER -> {
                loadThreadAndFrame();
                pushInt(node.site);
                load(node.getObject(), Repr.OBJ);
                mv.visitLdcInsn(node.name);
                mv.visitInsn(node.access == MemberAccess.SUPER ? ICONST_1 : ICONST_0);
                load(node.getValue(), Repr.OBJ);
                callUtils("storeExternal", "(" + THREAD_DESC + FRAME_DESC + "I" + TOBJECT_DESC
                        + "Ljava/lang/String;Z" + TOBJECT_DESC + ")V");
            }
        }
        return null;
    }

    @Override
    public Void visitContainerWrite(ContainerWrite node, Void unused) {
        loadThreadAndFrame();
        pushInt(node.site);
        load(node.getContainer(), Repr.OBJ);
        load(node.getKey(), Repr.OBJ);
        load(node.getValue(), Repr.OBJ);
        callUtils("containerWrite", "(" + THREAD_DESC + FRAME_DESC + "I" + TOBJECT_DESC + TOBJECT_DESC + TOBJECT_DESC + ")V");
        return null;
    }


    /* ------------------------------------------------------------------ */
    /*  values (each visit pushes the value in its representation)        */
    /* ------------------------------------------------------------------ */

    @Override
    public Void visitInteger(Int node, Void unused) {
        pushInt(node.value);
        return null;
    }

    @Override
    public Void visitReal(Real node, Void unused) {
        mv.visitLdcInsn(node.value);
        return null;
    }

    @Override
    public Void visitString(Str node, Void unused) {
        mv.visitLdcInsn(node.value);
        return null;
    }

    @Override
    public Void visitBool(Bool node, Void unused) {
        mv.visitInsn(node.value ? ICONST_1 : ICONST_0);
        return null;
    }

    @Override
    public Void visitNull(NullValue node, Void unused) {
        mv.visitFieldInsn(GETSTATIC, org.objectweb.asm.Type.getInternalName(Null.class), "INSTANCE",
                org.objectweb.asm.Type.getDescriptor(Null.class));
        return null;
    }

    @Override
    public Void visitConstant(Constant node, Void unused) {
        pushInt(node.index);
        callUtils("constant", "(I)" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitBuiltin(Builtin node, Void unused) {
        pushInt(node.index);
        mv.visitMethodInsn(INVOKESTATIC, org.objectweb.asm.Type.getInternalName(Builtins.class), "load", "(I)" + TOBJECT_DESC, false);
        return null;
    }

    @Override
    public Void visitType(com.tscript.runtime.jit.comp.ir.nodes.Type node, Void unused) {
        pushInt(node.index);
        callUtils("constant", "(I)" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitThis(This node, Void unused) {
        mv.visitVarInsn(ALOAD, FRAME_SLOT);
        mv.visitFieldInsn(GETFIELD, FRAME, "owner", TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitLocal(Local node, Void unused) {
        mv.visitVarInsn(ALOAD, FRAME_SLOT);
        pushInt(node.index);
        callUtils("local", "(" + FRAME_DESC + "I)" + TOBJECT_DESC);
        convert(Repr.OBJ, repr(node), typeMap.getType(node));
        return null;
    }

    @Override
    public Void visitStackValue(StackValue node, Void unused) {
        mv.visitVarInsn(ALOAD, FRAME_SLOT);
        pushInt(node.index);
        callUtils("stackValue", "(" + FRAME_DESC + "I)" + TOBJECT_DESC);
        convert(Repr.OBJ, repr(node), typeMap.getType(node));
        return null;
    }

    @Override
    public Void visitParameter(Parameter node, Void unused) {
        mv.visitVarInsn(ALOAD, FRAME_SLOT);
        pushInt(node.index);
        callUtils("argument", "(" + FRAME_DESC + "I)" + TOBJECT_DESC);
        convert(Repr.OBJ, repr(node), node.type);
        return null;
    }

    @Override
    public Void visitPhi(Phi node, Void unused) {
        throw new IllegalStateException("phis are loaded from their local variable");
    }

    @Override
    public Void visitPi(Pi node, Void unused) {
        load(node.getValue());
        convert(repr(node.getValue()), repr(node), node.type);
        return null;
    }

    @Override
    public Void visitAdd(Add node, Void unused) {
        if (repr(node) == Repr.STR) {
            loadAsString(node.getLeft());
            loadAsString(node.getRight());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
            return null;
        }
        arithmetic(node, IADD, DADD, Opcode.ADD);
        return null;
    }

    @Override
    public Void visitSub(Sub node, Void unused) {
        arithmetic(node, ISUB, DSUB, Opcode.SUB);
        return null;
    }

    @Override
    public Void visitMul(Mul node, Void unused) {
        arithmetic(node, IMUL, DMUL, Opcode.MUL);
        return null;
    }

    @Override
    public Void visitDiv(Div node, Void unused) {
        arithmetic(node, -1, DDIV, Opcode.DIV);
        return null;
    }

    @Override
    public Void visitIDiv(IDiv node, Void unused) {
        arithmetic(node, IDIV, -1, Opcode.IDIV);
        return null;
    }

    @Override
    public Void visitMod(Mod node, Void unused) {
        arithmetic(node, IREM, -1, Opcode.MOD);
        return null;
    }

    @Override
    public Void visitPow(Pow node, Void unused) {
        Repr repr = repr(node);
        if (repr == Repr.INT || repr == Repr.REAL) {
            load(node.getLeft(), Repr.REAL);
            load(node.getRight(), Repr.REAL);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
            if (repr == Repr.INT) mv.visitInsn(D2I);
        }
        else {
            genericBinary(node, Opcode.POW);
        }
        return null;
    }

    @Override
    public Void visitShl(Shl node, Void unused) {
        arithmetic(node, ISHL, -1, Opcode.SHL);
        return null;
    }

    @Override
    public Void visitShr(Shr node, Void unused) {
        arithmetic(node, ISHR, -1, Opcode.SHR);
        return null;
    }

    @Override
    public Void visitUshr(Ushr node, Void unused) {
        arithmetic(node, IUSHR, -1, Opcode.USHR);
        return null;
    }

    @Override
    public Void visitAnd(And node, Void unused) {
        arithmetic(node, IAND, -1, Opcode.AND);
        return null;
    }

    @Override
    public Void visitOr(Or node, Void unused) {
        arithmetic(node, IOR, -1, Opcode.OR);
        return null;
    }

    @Override
    public Void visitXor(Xor node, Void unused) {
        arithmetic(node, IXOR, -1, Opcode.XOR);
        return null;
    }

    @Override
    public Void visitLt(Lt node, Void unused) {
        // DCMPG yields 1 for NaN, so comparisons with NaN are false
        comparison(node, IF_ICMPGE, IFGE, Opcode.LT);
        return null;
    }

    @Override
    public Void visitLeq(Leq node, Void unused) {
        comparison(node, IF_ICMPGT, IFGT, Opcode.LEQ);
        return null;
    }

    @Override
    public Void visitEquals(Equals node, Void unused) {
        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();
        IRType lt = typeMap.getType(lhs);
        IRType rt = typeMap.getType(rhs);

        if (IRType.isPrimitive(lt) && IRType.isPrimitive(rt) && !lt.equals(rt)) {
            // objects of different classes are never equal
            mv.visitInsn(ICONST_0);
            return null;
        }

        Repr l = repr(lhs), r = repr(rhs);
        if (l == Repr.INT && r == Repr.INT || l == Repr.BOOL && r == Repr.BOOL) {
            load(lhs);
            load(rhs);
            materializeBoolean(IF_ICMPNE);
        }
        else if (l == Repr.REAL && r == Repr.REAL) {
            // Double#equals semantics: NaN equals NaN, 0.0 does not equal -0.0
            load(lhs);
            load(rhs);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "compare", "(DD)I", false);
            materializeBoolean(IFNE);
        }
        else if (l == Repr.STR && r == Repr.STR) {
            load(lhs);
            load(rhs);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        }
        else {
            // the interpreter evaluates rhs.equals(lhs)
            load(rhs, Repr.OBJ);
            load(lhs, Repr.OBJ);
            callUtils("areEqual", "(" + TOBJECT_DESC + TOBJECT_DESC + ")Z");
        }
        return null;
    }

    @Override
    public Void visitNot(Not node, Void unused) {
        Repr repr = repr(node);
        if (repr == Repr.INT) {
            load(node.getValue(), Repr.INT);
            mv.visitInsn(ICONST_M1);
            mv.visitInsn(IXOR);
        }
        else if (repr == Repr.BOOL) {
            load(node.getValue(), Repr.BOOL);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IXOR);
        }
        else {
            genericUnary(node, Opcode.NOT);
        }
        return null;
    }

    @Override
    public Void visitNeg(Neg node, Void unused) {
        Repr repr = repr(node);
        if (repr == Repr.INT) {
            load(node.getValue(), Repr.INT);
            mv.visitInsn(INEG);
        }
        else if (repr == Repr.REAL) {
            load(node.getValue(), Repr.REAL);
            mv.visitInsn(DNEG);
        }
        else {
            genericUnary(node, Opcode.NEG);
        }
        return null;
    }

    @Override
    public Void visitPos(Pos node, Void unused) {
        Repr repr = repr(node);
        if (repr == Repr.INT || repr == Repr.REAL) {
            load(node.getValue(), repr);
        }
        else {
            genericUnary(node, Opcode.POS);
        }
        return null;
    }

    @Override
    public Void visitTypeof(Typeof node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        mv.visitMethodInsn(INVOKEINTERFACE, TOBJECT, "getType",
                "()" + org.objectweb.asm.Type.getDescriptor(com.tscript.runtime.typing.Type.class), true);
        return null;
    }

    @Override
    public Void visitIsType(IsType node, Void unused) {
        IRValue value = node.getValue();
        IRType actual = typeMap.getType(value);
        if (IRType.isExact(actual)) {
            mv.visitInsn(actual.equals(node.type) ? ICONST_1 : ICONST_0);
            return null;
        }

        load(value, Repr.OBJ);
        IRType type = node.type;
        if (type == NullType.INSTANCE) {
            callUtils("isNull", "(" + TOBJECT_DESC + ")Z");
        }
        else {
            com.tscript.runtime.typing.Type runtimeType = IRType.runtimeTypeOf(type);
            Class<?> clazz = runtimeClassOf(type);
            if (clazz != null) {
                mv.visitTypeInsn(INSTANCEOF, org.objectweb.asm.Type.getInternalName(clazz));
            }
            else if (runtimeType != null) {
                mv.visitMethodInsn(INVOKEINTERFACE, TOBJECT, "getType",
                        "()" + org.objectweb.asm.Type.getDescriptor(com.tscript.runtime.typing.Type.class), true);
                pushInt(ObjectCache.putId(runtimeType));
                callUtils("constant", "(I)" + TOBJECT_DESC);
                materializeBoolean(IF_ACMPNE);
            }
            else {
                throw new UnsupportedOperationException("type check for " + type);
            }
        }
        return null;
    }

    private static Class<?> runtimeClassOf(IRType type) {
        if (type == IntType.INSTANCE) return com.tscript.runtime.typing.TInteger.class;
        if (type == RealType.INSTANCE) return com.tscript.runtime.typing.TReal.class;
        if (type == BoolType.INSTANCE) return com.tscript.runtime.typing.TBoolean.class;
        if (type == StrType.INSTANCE) return com.tscript.runtime.typing.TString.class;
        if (type == RangeIteratorType.INSTANCE) return RangeIterator.class;
        return null;
    }

    @Override
    public Void visitTruthy(Truthy node, Void unused) {
        IRValue value = node.getValue();
        switch (repr(value)) {
            case BOOL -> load(value);
            case INT -> {
                load(value);
                materializeBoolean(IFEQ);
            }
            case REAL -> mv.visitInsn(ICONST_1);
            case STR -> {
                load(value);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "isEmpty", "()Z", false);
                materializeBoolean(IFNE);
            }
            case OBJ -> {
                load(value);
                callUtils("isTrue", "(" + TOBJECT_DESC + ")Z");
            }
        }
        return null;
    }

    @Override
    public Void visitIsSelf(IsSelf node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        mv.visitVarInsn(ALOAD, FRAME_SLOT);
        callUtils("isSelf", "(" + TOBJECT_DESC + FRAME_DESC + ")Z");
        return null;
    }

    @Override
    public Void visitRange(Range node, Void unused) {
        if (repr(node.getFrom()) == Repr.INT && repr(node.getTo()) == Repr.INT) {
            load(node.getFrom());
            load(node.getTo());
            callUtils("range", "(II)" + TOBJECT_DESC);
        }
        else {
            load(node.getFrom(), Repr.INT);
            load(node.getTo(), Repr.INT);
            callUtils("range", "(II)" + TOBJECT_DESC);
        }
        return null;
    }

    @Override
    public Void visitArray(Array node, Void unused) {
        loadObjectArray(node.getValues());
        callUtils("newArray", "([" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitDict(Dict node, Void unused) {
        loadObjectArray(node.getInputs());
        callUtils("newDict", "([" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitCall(Call node, Void unused) {
        loadThreadAndFrame();
        pushInt(node.site);
        load(node.getCalled(), Repr.OBJ);
        loadObjectArray(node.getArgs());
        mv.visitMethodInsn(INVOKESTATIC, org.objectweb.asm.Type.getInternalName(ImageRunner.class), "invoke",
                "(" + THREAD_DESC + FRAME_DESC + "I" + TOBJECT_DESC + "[" + TOBJECT_DESC + ")" + TOBJECT_DESC, false);
        return null;
    }

    @Override
    public Void visitGlobal(Global node, Void unused) {
        pushInt(node.global.moduleId());
        pushInt(node.global.index());
        callUtils("loadGlobal", "(II)" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitGetIterator(GetIterator node, Void unused) {
        loadThreadAndFrame();
        pushInt(siteOf(node));
        load(node.getValue(), Repr.OBJ);
        callUtils("getIterator", "(" + THREAD_DESC + FRAME_DESC + "I" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        return null;
    }

    private static final String RANGE_ITERATOR = org.objectweb.asm.Type.getInternalName(RangeIterator.class);

    private boolean isRangeIterator(IRValue value) {
        return typeMap.getType(value) == RangeIteratorType.INSTANCE;
    }

    @Override
    public Void visitIterHasNext(IterHasNext node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        if (isRangeIterator(node.getValue())) {
            // iterator.current < iterator.end
            mv.visitTypeInsn(CHECKCAST, RANGE_ITERATOR);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETFIELD, RANGE_ITERATOR, "current", "I");
            mv.visitInsn(SWAP);
            mv.visitFieldInsn(GETFIELD, RANGE_ITERATOR, "end", "I");
            materializeBoolean(IF_ICMPGE);
        }
        else {
            callUtils("hasNext", "(" + TOBJECT_DESC + ")Z");
        }
        return null;
    }

    @Override
    public Void visitIterNext(IterNext node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        if (isRangeIterator(node.getValue())) {
            // iterator.current++ (yields the old value)
            mv.visitTypeInsn(CHECKCAST, RANGE_ITERATOR);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETFIELD, RANGE_ITERATOR, "current", "I");
            mv.visitInsn(DUP_X1);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitFieldInsn(PUTFIELD, RANGE_ITERATOR, "current", "I");
        }
        else {
            callUtils("next", "(" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        }
        return null;
    }

    @Override
    public Void visitIsCallee(IsCallee node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        pushInt(node.metadataId);
        callUtils("isCallee", "(" + TOBJECT_DESC + "I)Z");
        return null;
    }

    @Override
    public Void visitFunctionOwner(FunctionOwner node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        callUtils("functionOwner", "(" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitNewRangeIterator(NewRangeIterator node, Void unused) {
        mv.visitTypeInsn(NEW, RANGE_ITERATOR);
        mv.visitInsn(DUP);
        load(node.getLeft(), Repr.INT);
        load(node.getRight(), Repr.INT);
        mv.visitMethodInsn(INVOKESPECIAL, RANGE_ITERATOR, "<init>", "(II)V", false);
        return null;
    }

    @Override
    public Void visitContainerRead(ContainerRead node, Void unused) {
        loadThreadAndFrame();
        pushInt(siteOf(node));
        load(node.getContainer(), Repr.OBJ);
        load(node.getKey(), Repr.OBJ);
        callUtils("containerRead", "(" + THREAD_DESC + FRAME_DESC + "I" + TOBJECT_DESC + TOBJECT_DESC + ")" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitLoadMember(LoadMember node, Void unused) {
        switch (node.access) {
            case FIELD -> {
                load(node.getObject(), Repr.OBJ);
                pushInt(node.index);
                callUtils("loadField", "(" + TOBJECT_DESC + "I)" + TOBJECT_DESC);
            }
            case INTERNAL -> {
                load(node.getObject(), Repr.OBJ);
                pushInt(node.index);
                callUtils("loadInternal", "(" + TOBJECT_DESC + "I)" + TOBJECT_DESC);
            }
            case EXTERNAL, SUPER -> {
                loadThreadAndFrame();
                pushInt(siteOf(node));
                load(node.getObject(), Repr.OBJ);
                mv.visitLdcInsn(node.name);
                mv.visitInsn(node.access == MemberAccess.SUPER ? ICONST_1 : ICONST_0);
                callUtils("loadExternal", "(" + THREAD_DESC + FRAME_DESC + "I" + TOBJECT_DESC + "Ljava/lang/String;Z)" + TOBJECT_DESC);
            }
        }
        return null;
    }

    @Override
    public Void visitNewObject(NewObject node, Void unused) {
        pushInt(node.typeId);
        callUtils("newInstance", "(I)" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitNewFunction(NewFunction node, Void unused) {
        pushInt(node.moduleId);
        pushInt(node.index);
        if (node.getOwner() != null) load(node.getOwner(), Repr.OBJ);
        else mv.visitInsn(ACONST_NULL);
        callUtils("newFunction", "(II" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        return null;
    }

    @Override
    public Void visitIsIdentical(IsIdentical node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        pushInt(node.objectId);
        callUtils("isIdentical", "(" + TOBJECT_DESC + "I)Z");
        return null;
    }

    @Override
    public Void visitGetSuper(GetSuper node, Void unused) {
        load(node.getValue(), Repr.OBJ);
        mv.visitMethodInsn(INVOKEINTERFACE, TOBJECT, "getSuper", "()" + TOBJECT_DESC, true);
        return null;
    }

    private int siteOf(IRValue fixed) {
        for (IRNode user : fixed.getUsers()) {
            if (user instanceof Anchor anchor) return anchor.site;
        }
        return -1;
    }

}
