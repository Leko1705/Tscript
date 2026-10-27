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
    private static final String TOBJECT = "com/tscript/runtime/typing/TObject";
    private static final String TOBJECT_DESC = "L" + TOBJECT + ";";
    private static final String THREAD_DESC = org.objectweb.asm.Type.getDescriptor(TThread.class);
    private static final String FRAME = org.objectweb.asm.Type.getInternalName(ImageFrame.class);
    private static final String FRAME_DESC = "L" + FRAME + ";";

    private final JBCImageGenerator generator;
    private final MethodVisitor mv;
    private final Graph graph;
    private final Graph.TypeMap typeMap;

    private Map<IRValue, Integer> slots = new HashMap<>();
    private int nextSlot = FIRST_FREE_SLOT;
    private Label loopLabel = null;

    public EmitterImpl(String className, Graph graph) {
        this.generator = new JBCImageGenerator(className);
        this.mv = generator.getMethodVisitor();
        this.graph = graph;
        this.typeMap = graph.typeMap();
    }

    public byte[] emit() {
        IRControl exit = graph.exitControl();
        if (exit == null) {
            throw new IllegalStateException("IRGraph must have an exit control");
        }
        if (!(exit instanceof Return) && !(exit instanceof LoopEndNode) && !(exit instanceof Exit)) {
            throw new UnsupportedOperationException("unsupported exit control " + exit);
        }

        typeMap.flush();
        emitPrologue();

        for (IREffect effect : graph.effects()) {
            effect.accept(this);
        }

        if (exit instanceof Return ret) {
            load(ret.getValue(), Repr.OBJ);
            mv.visitInsn(ARETURN);
        }
        else if (exit instanceof Exit x) {
            if (x.getDeoptInfo() == null) x.setDeoptInfo(new DeoptInfo(x.getState(), false, true, false));
            emitLeave(x, (DeoptInfo) x.getDeoptInfo());
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

        return generator.generate();
    }

    /**
     * Loads all entry values (state of the interpreter frame or arguments) into local variables.
     */
    private void emitPrologue() {
        for (IRValue value : graph.values()) {
            if (value instanceof Local || value instanceof StackValue || value instanceof Parameter) {
                value.accept(this);
                store(value);
            }
        }
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
        int slot = nextSlot;
        nextSlot += repr.size;
        if (nextSlot > 65000) throw new IllegalStateException("too many local variables");
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
        load(value);
        convert(repr(value), target, typeMap.getType(value));
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
        if (node.getDeoptInfo() == null) {
            node.setDeoptInfo(new DeoptInfo(node.getState(), node.isLoopExit(), false, node.isSideTraceable()));
        }
        emitLeave(node, (DeoptInfo) node.getDeoptInfo());
        slots = saved;

        mv.visitLabel(ok);
        return null;
    }

    /**
     * Leaves compiled code: writes back promoted globals and returns a deoptimization.
     */
    private void emitLeave(StateCarrier carrier, DeoptInfo info) {
        emitGlobalWriteBacks(carrier);
        pushInt(ObjectCache.putId(info));
        loadObjectArray(carrier.getStateValues());
        callUtils("deopt", "(I[" + TOBJECT_DESC + ")" + TOBJECT_DESC);
        mv.visitInsn(ARETURN);
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

        for (Phi phi : loop.getPhis()) {
            load(phi.getEntry(), repr(phi));
            store(phi);
        }
        loopLabel = new Label();
        mv.visitLabel(loopLabel);
        return null;
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

    private int siteOf(IRValue fixed) {
        for (IRNode user : fixed.getUsers()) {
            if (user instanceof Anchor anchor) return anchor.site;
        }
        return -1;
    }

}
