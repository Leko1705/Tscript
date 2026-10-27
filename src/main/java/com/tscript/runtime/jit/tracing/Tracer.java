package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.ParseProcess;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;

import java.util.*;

/**
 * Records the execution of the interpreter into a {@link Graph} (a trace).
 * <p>
 * The tracer decorates the interpreter: every instruction is first recorded and then executed
 * by the underlying interpreter. Only the instructions of the traced frame are recorded;
 * calls are recorded as {@link Call} nodes and the called functions are executed without
 * recording them.
 * <p>
 * There are two kinds of traces:
 * <ul>
 *     <li><b>function traces</b> start at the first instruction of a function and end at its
 *     return. A self tail call ({@code return f(...)} inside {@code f}) is turned into a loop:
 *     the function entry is a loop header whose phis are the parameters. Other (recursive) calls
 *     stay calls.</li>
 *     <li><b>loop traces</b> start at the header of a hot loop and end when execution jumps back
 *     to the header.</li>
 * </ul>
 * Whenever execution passes a loop header, a {@link LoopHeadNode} is opened and phis are created
 * eagerly for every local variable and stack slot. When execution jumps back to an open header,
 * the loop is closed (the second input of every phi is set to the value at the end of the body)
 * and the trace is complete. Headers that are never jumped back to are degenerate and their
 * phis collapse to their entry values ({@link Graph#finish()}).
 * <p>
 * Every speculation (taken branch direction, operand types, ...) is protected by a guard that
 * captures the interpreter state before the current instruction. If the guard fails at runtime,
 * the interpreter re-executes that instruction.
 */
public class Tracer extends InterpreterDecorator {

    private enum Kind { FUNCTION, LOOP, SIDE }

    private record State(FrameState frameState, List<IRValue> locals, List<IRValue> stack) {}

    private record PendingCall(IRValue call, int expectedIp, int expectedSp) {}

    private static final class OpenLoop {
        final LoopHeadNode loop;
        final int ip;
        final int stackSize;
        final Map<Integer, Phi> localPhis = new HashMap<>();
        final List<Phi> stackPhis = new ArrayList<>();
        final int firstGuard;

        OpenLoop(LoopHeadNode loop, int ip, int stackSize, int firstGuard) {
            this.loop = loop;
            this.ip = ip;
            this.stackSize = stackSize;
            this.firstGuard = firstGuard;
        }
    }

    private final Interpreter restore;
    private final ParseProcess process;
    private final Graph graph;
    private final Graph.Builder builder;
    private final Kind kind;
    private final int anchorIp;
    private final int traceDepth;
    private final int lengthLimit;
    private final boolean allowReturn;

    private boolean started = false;
    private boolean finished = false;

    private VirtualFrame frame;
    private VirtualFunction.VirtualMetadata metadata;
    private Set<Integer> loopHeaders;

    /** current values of the local variables (only defined ones) */
    private final Map<Integer, IRValue> locals = new HashMap<>();
    /** current values of the operand stack (bottom first) */
    private final List<IRValue> stack = new ArrayList<>();
    /** type narrowed versions of values established by type guards */
    private final Map<IRValue, Pi> narrowed = new HashMap<>();

    private final Map<Integer, OpenLoop> openLoops = new HashMap<>();
    private OpenLoop entryLoop = null;

    /** recorded branch guards and the address execution would continue at if they fail */
    private final List<GuardBool> branchGuards = new ArrayList<>();
    private final List<Integer> branchAlternatives = new ArrayList<>();

    private PendingCall pendingCall = null;
    private int line = -1;
    private int ip;
    private int recorded = 0;


    private Tracer(Interpreter interpreter, Interpreter restore, ParseProcess process, Kind kind, int anchorIp, int traceDepth, boolean allowReturn) {
        super(interpreter);
        this.restore = restore;
        this.process = process;
        this.graph = process.getGraph();
        this.builder = graph.builder();
        this.kind = kind;
        this.anchorIp = anchorIp;
        this.traceDepth = traceDepth;
        this.lengthLimit = process.getCompiler().getConfiguration().traceLengthLimit();
        this.allowReturn = allowReturn;
    }

    static Tracer forFunction(Interpreter interpreter, Interpreter restore, ParseProcess process, int traceDepth) {
        return new Tracer(interpreter, restore, process, Kind.FUNCTION, 0, traceDepth, true);
    }

    static Tracer forLoop(Interpreter interpreter, Interpreter restore, ParseProcess process, int headerIp, int traceDepth) {
        return new Tracer(interpreter, restore, process, Kind.LOOP, headerIp, traceDepth, false);
    }

    /**
     * Creates a tracer for a side trace: a trace that starts where another trace was left at a
     * guard and that continues until the function returns (only if {@code allowReturn}) or until
     * execution jumps back to a loop header.
     */
    static Tracer forSide(Interpreter interpreter, Interpreter restore, ParseProcess process, int ip, int traceDepth, boolean allowReturn) {
        return new Tracer(interpreter, restore, process, Kind.SIDE, ip, traceDepth, allowReturn);
    }


    /* ------------------------------------------------------------------ */
    /*  recording infrastructure                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Must be called at the beginning of every instruction.
     * @return true if the current instruction belongs to the traced frame and has to be recorded
     */
    private boolean record() {
        if (finished) return false;

        TThread thread = getCurrentThread();
        int depth = thread.frameStack.size();

        if (!thread.isRunning() || depth < traceDepth) {
            abort("the traced frame was left unexpectedly");
            return false;
        }
        if (depth > traceDepth) {
            // inside a called function
            return false;
        }

        if (!(thread.getFrame() instanceof VirtualFrame top)) {
            abort("unexpected native frame");
            return false;
        }
        if (!started) {
            start(top);
            if (finished) return false;
        }
        else if (top != frame) {
            abort("unexpected frame");
            return false;
        }

        ip = frame.ip - 1;

        if (pendingCall != null) {
            if (ip != pendingCall.expectedIp || frame.sp != pendingCall.expectedSp) {
                abort("call did not return regularly");
                return false;
            }
            stack.add(pendingCall.call);
            pendingCall = null;
        }

        if (++recorded > lengthLimit) {
            abort("trace too long");
            return false;
        }

        if (loopHeaders.contains(ip) && !openLoops.containsKey(ip)) {
            openLoop(ip);
        }

        return true;
    }

    private void start(VirtualFrame top) {
        started = true;
        frame = top;
        metadata = top.getMetadata();
        loopHeaders = LoopAnalysis.loopHeaders(metadata);
        ip = top.ip - 1;
        line = top.line();
        builder.setLine(line);

        if (top.inUnsafeSpot()) {
            abort("can not trace inside a try block");
            return;
        }

        if (kind == Kind.FUNCTION) {
            if (ip != 0) {
                abort("function trace does not start at the function entry");
                return;
            }
            // the arguments are on the stack, the first argument on top
            for (int i = 0; i < top.sp; i++) {
                int paramIndex = top.sp - 1 - i;
                IRType type = IRType.from(top.stack[i], false);
                stack.add(builder.parameter(paramIndex, type));
            }
            // the function entry is a loop header for self tail calls
            entryLoop = openLoop(0);
        }
        else {
            if (ip != anchorIp) {
                abort("trace does not start at its anchor");
                return;
            }

            for (int i = 0; i < top.sp; i++) {
                stack.add(builder.stackValue(i));
            }

            State entryState = state();
            for (int i = 0; i < metadata.locals(); i++) {
                TObject value = top.locals[i];
                if (value == null) continue;
                IRValue local = builder.local(i);
                IRType type = IRType.from(value, false);
                if (IRType.isPrimitive(type)) {
                    // speculate on the type of the local, so that it can be kept unboxed in the loop
                    GuardBool guard = builder.guard(builder.isType(local, type),
                            entryState.frameState, entryState.locals, entryState.stack);
                    Pi pi = (Pi) builder.pi(local, type);
                    guard.setNarrowed(pi);
                    guard.setSideTraceable(false);
                    local = pi;
                }
                locals.put(i, local);
            }
            // the header itself is opened by record()
        }
    }

    private void complete() {
        finished = true;
        getCurrentThread().setInterpreter(restore);
        try {
            graph.finish();
            process.getCompiler().debug(() -> "recorded trace of " + process.getEntry() + "\n" + graph);
        }
        catch (RuntimeException e) {
            process.abort("invalid trace: " + e);
            return;
        }
        process.compile();
    }

    private void abort(String reason) {
        if (finished) return;
        finished = true;
        getCurrentThread().setInterpreter(restore);
        process.abort(reason + " (at ip " + ip + ")");
    }

    private State state() {
        List<Integer> indices = new ArrayList<>(locals.keySet());
        Collections.sort(indices);
        int[] localIndices = new int[indices.size()];
        List<IRValue> localValues = new ArrayList<>(indices.size());
        for (int i = 0; i < indices.size(); i++) {
            localIndices[i] = indices.get(i);
            localValues.add(locals.get(indices.get(i)));
        }
        FrameState frameState = new FrameState(ip, line, localIndices, stack.size());
        return new State(frameState, localValues, new ArrayList<>(stack));
    }

    private GuardBool guard(IRValue condition, State state) {
        return builder.guard(condition, state.frameState, state.locals, state.stack);
    }

    private void push(IRValue value) {
        stack.add(value);
    }

    private IRValue popValue() {
        return stack.remove(stack.size() - 1);
    }

    private IRValue peek() {
        return stack.get(stack.size() - 1);
    }

    private TObject runtimeTop(int offset) {
        return frame.stack[frame.sp - 1 - offset];
    }

    private IRType typeOf(IRValue value) {
        return graph.typeMap().getType(value);
    }

    /**
     * Speculates that the given value has the (primitive) type of the given runtime object.
     * @return the narrowed value
     */
    private IRValue narrow(IRValue value, TObject runtime, State state) {
        IRType type = IRType.from(runtime, false);
        if (!IRType.isPrimitive(type)) return value;

        IRType known = typeOf(value);
        if (known.equals(type)) return value;

        Pi existing = narrowed.get(value);
        if (existing != null && existing.type.equals(type)) return existing;

        guard(builder.isType(value, type), state);
        IRValue pi = builder.pi(value, type);
        if (pi instanceof Pi p) {
            narrowed.put(value, p);
        }

        // later uses of the value can use the narrowed version as well
        locals.replaceAll((k, v) -> v == value ? pi : v);
        stack.replaceAll(v -> v == value ? pi : v);
        return pi;
    }

    private static boolean isNumeric(IRType type) {
        return type == IntType.INSTANCE || type == RealType.INSTANCE;
    }

    private OpenLoop openLoop(int headerIp) {
        LoopHeadNode loop = builder.beginLoop(headerIp);
        OpenLoop open = new OpenLoop(loop, headerIp, stack.size(), branchGuards.size());

        for (Map.Entry<Integer, IRValue> local : locals.entrySet()) {
            Phi phi = builder.phi(loop, local.getValue());
            open.localPhis.put(local.getKey(), phi);
            local.setValue(phi);
        }
        for (int i = 0; i < stack.size(); i++) {
            Phi phi = builder.phi(loop, stack.get(i));
            open.stackPhis.add(phi);
            stack.set(i, phi);
        }

        openLoops.put(headerIp, open);
        return open;
    }

    /**
     * Closes the loop of the given header (execution jumps back to it from the current instruction)
     * and completes the trace.
     */
    private void closeLoop(OpenLoop open, int backEdgeIp) {
        if (stack.size() != open.stackSize) {
            abort("inconsistent stack at loop back edge");
            return;
        }

        for (Map.Entry<Integer, Phi> entry : open.localPhis.entrySet()) {
            IRValue current = locals.get(entry.getKey());
            entry.getValue().setBackedge(current != null ? current : entry.getValue());
        }
        for (int i = 0; i < open.stackPhis.size(); i++) {
            open.stackPhis.get(i).setBackedge(stack.get(i));
        }

        // guards of branches leaving the loop are the regular loop exits
        for (int i = open.firstGuard; i < branchGuards.size(); i++) {
            int alternative = branchAlternatives.get(i);
            if (alternative < open.ip || alternative > backEdgeIp) {
                branchGuards.get(i).setLoopExit(true);
            }
        }

        builder.closeLoop(open.loop);
        complete();
    }

    /**
     * Handles a jump of the traced frame from the current instruction to the given target.
     */
    private void onJump(int target) {
        if (target > ip) return;
        OpenLoop open = openLoops.get(target);
        if (open != null) {
            closeLoop(open, ip);
        }
        else if (kind == Kind.SIDE) {
            // continue in the interpreter at the loop header (where e.g. the image of the loop can take over)
            State state = state();
            FrameState atTarget = new FrameState(target, line, state.frameState.localIndices(), state.frameState.stackSize());
            builder.exit(atTarget, state.locals, state.stack);
            complete();
        }
        else {
            abort("back edge to a loop header outside of the trace");
        }
    }

    /**
     * Records the current instruction (if it belongs to the traced frame). Internal errors
     * during recording abort the trace instead of affecting the executed program.
     */
    private void rec(Runnable recording) {
        if (record()) {
            try {
                recording.run();
            }
            catch (RuntimeException e) {
                if (process.getCompiler().getConfiguration().debug()) e.printStackTrace(System.err);
                abort("internal error while recording: " + e);
            }
        }
    }

    private void unsupported(String what) {
        rec(() -> {
            abort("unsupported instruction " + what);
        });
    }


    /* ------------------------------------------------------------------ */
    /*  recorded instructions                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void pushNull() {
        rec(() -> push(builder.nullValue()));
        super.pushNull();
    }

    @Override
    public void pushInt(byte value) {
        rec(() -> push(builder.integer(value)));
        super.pushInt(value);
    }

    @Override
    public void pushBool(byte value) {
        rec(() -> push(builder.bool(value == 1)));
        super.pushBool(value);
    }

    @Override
    public void loadConst(byte b1, byte b2) {
        rec(() -> {
            Pool pool = frame.getModule().getPool();
            push(constant(pool.loadConstant(b1, b2)));
        });
        super.loadConst(b1, b2);
    }

    private IRValue constant(TObject obj) {
        if (obj instanceof TString s) return builder.string(s.getValue());
        if (obj instanceof TReal r) return builder.real(r.getValue());
        if (obj instanceof TBoolean b) return builder.bool(b.getValue());
        if (obj instanceof TInteger i) return builder.integer(i.getValue());
        if (obj == com.tscript.runtime.typing.Null.INSTANCE) return builder.nullValue();
        return builder.constant(obj);
    }

    @Override
    public void pushThis() {
        rec(() -> push(builder.thisValue()));
        super.pushThis();
    }

    @Override
    public void loadBuiltin(byte b1, byte b2) {
        rec(() -> push(builder.builtin(Conversion.from2Bytes(b1, b2))));
        super.loadBuiltin(b1, b2);
    }

    @Override
    public void pop() {
        rec(() -> pop0());
        super.pop();
    }

    private void pop0() {
        popValue();
    }

    @Override
    public void dup() {
        rec(() -> push(peek()));
        super.dup();
    }

    @Override
    public void newLine(int line) {
        rec(() -> {
            this.line = line;
            builder.setLine(line);
        });
        super.newLine(line);
    }

    @Override
    public void loadGlobal(byte address) {
        rec(() -> push(builder.loadGlobal(address)));
        super.loadGlobal(address);
    }

    @Override
    public void storeGlobal(byte address) {
        rec(() -> builder.storeGlobal(address, popValue()));
        super.storeGlobal(address);
    }

    @Override
    public void loadLocal(byte address) {
        rec(() -> {
            IRValue value = locals.get((int) address);
            if (value == null) {
                abort("load of undefined local");
            }
            else {
                push(value);
            }
        });
        super.loadLocal(address);
    }

    @Override
    public void storeLocal(byte address) {
        rec(() -> locals.put((int) address, popValue()));
        super.storeLocal(address);
    }

    @Override
    public void binaryOperation(Opcode opcode) {
        rec(() -> recordBinaryOperation(opcode));
        super.binaryOperation(opcode);
    }

    private void recordBinaryOperation(Opcode opcode) {
        State state = state();
        TObject rightObj = runtimeTop(0);
        TObject leftObj = runtimeTop(1);

        IRType lt = IRType.from(leftObj, false);
        IRType rt = IRType.from(rightObj, false);

        boolean valid = switch (opcode) {
            case ADD -> (isNumeric(lt) && isNumeric(rt))
                    || (lt == StrType.INSTANCE && IRType.isPrimitive(rt))
                    || (rt == StrType.INSTANCE && IRType.isPrimitive(lt));
            case SUB, MUL, DIV, POW, LT, GT, LEQ, GEQ -> isNumeric(lt) && isNumeric(rt);
            case IDIV, MOD, SHL, SHR, USHR -> lt == IntType.INSTANCE && rt == IntType.INSTANCE;
            case AND, OR, XOR -> (lt == IntType.INSTANCE && rt == IntType.INSTANCE)
                    || (lt == BoolType.INSTANCE && rt == BoolType.INSTANCE);
            default -> false;
        };

        if (!valid) {
            abort("unsupported operand types " + lt + " " + opcode + " " + rt);
            return;
        }

        IRValue rhs = narrow(popValue(), rightObj, state);
        IRValue lhs = narrow(popValue(), leftObj, state);

        IRValue result = switch (opcode) {
            case ADD -> builder.add(lhs, rhs);
            case SUB -> builder.sub(lhs, rhs);
            case MUL -> builder.mul(lhs, rhs);
            case DIV -> builder.div(lhs, rhs);
            case IDIV -> builder.idiv(lhs, rhs);
            case MOD -> builder.mod(lhs, rhs);
            case POW -> builder.pow(lhs, rhs);
            case AND -> builder.and(lhs, rhs);
            case OR -> builder.or(lhs, rhs);
            case XOR -> builder.xor(lhs, rhs);
            case SHL -> builder.shl(lhs, rhs);
            case SHR -> builder.shr(lhs, rhs);
            case USHR -> builder.ushr(lhs, rhs);
            case LT -> builder.lt(lhs, rhs);
            case GT -> builder.gt(lhs, rhs);
            case LEQ -> builder.leq(lhs, rhs);
            case GEQ -> builder.geq(lhs, rhs);
            default -> throw new AssertionError("Unknown opcode: " + opcode);
        };

        push(result);
    }

    @Override
    public void equals(boolean shouldBeEqual) {
        rec(() -> {
            State state = state();
            IRValue rhs = narrow(popValue(), runtimeTop(0), state);
            IRValue lhs = narrow(popValue(), runtimeTop(1), state);
            IRValue equals = builder.equals(lhs, rhs);
            push(shouldBeEqual ? equals : builder.not(equals));
        });
        super.equals(shouldBeEqual);
    }

    @Override
    public void not() {
        rec(() -> {
            TObject obj = runtimeTop(0);
            IRType type = IRType.from(obj, false);
            if (type != IntType.INSTANCE && type != BoolType.INSTANCE) {
                abort("unsupported operand type for not: " + type);
            }
            else {
                push(builder.not(narrow(popValue(), obj, state())));
            }
        });
        super.not();
    }

    @Override
    public void negate() {
        rec(() -> {
            TObject obj = runtimeTop(0);
            IRType type = IRType.from(obj, false);
            if (!isNumeric(type)) {
                abort("unsupported operand type for negation: " + type);
            }
            else {
                push(builder.neg(narrow(popValue(), obj, state())));
            }
        });
        super.negate();
    }

    @Override
    public void posivate() {
        rec(() -> {
            TObject obj = runtimeTop(0);
            IRType type = IRType.from(obj, false);
            if (!isNumeric(type)) {
                abort("unsupported operand type for unary plus: " + type);
            }
            else {
                push(builder.pos(narrow(popValue(), obj, state())));
            }
        });
        super.posivate();
    }

    @Override
    public void getType() {
        rec(() -> push(builder.typeof(popValue())));
        super.getType();
    }

    @Override
    public void makeArray(byte size) {
        rec(() -> {
            List<IRValue> values = new ArrayList<>();
            for (int i = 0; i < size; i++) values.add(popValue());
            push(builder.array(values));
        });
        super.makeArray(size);
    }

    @Override
    public void makeDict(byte size) {
        rec(() -> {
            List<IRValue> keys = new ArrayList<>();
            List<IRValue> values = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                values.add(popValue());
                keys.add(popValue());
            }
            push(builder.dict(keys, values));
        });
        super.makeDict(size);
    }

    @Override
    public void makeRange() {
        rec(() -> {
            TObject to = runtimeTop(0);
            TObject from = runtimeTop(1);
            if (!(to instanceof TInteger) || !(from instanceof TInteger)) {
                abort("invalid range bounds");
            }
            else {
                State state = state();
                IRValue toValue = narrow(popValue(), to, state);
                IRValue fromValue = narrow(popValue(), from, state);
                push(builder.range(fromValue, toValue));
            }
        });
        super.makeRange();
    }

    @Override
    public void containerRead() {
        rec(() -> {
            IRValue container = popValue();
            IRValue key = popValue();
            push(builder.containerRead(container, key));
        });
        super.containerRead();
    }

    @Override
    public void containerWrite() {
        rec(() -> {
            IRValue container = popValue();
            IRValue key = popValue();
            IRValue value = popValue();
            builder.containerWrite(container, key, value);
        });
        super.containerWrite();
    }

    @Override
    public void jumpTo(byte b1, byte b2) {
        rec(() -> onJump(Conversion.from2Bytes(b1, b2)));
        super.jumpTo(b1, b2);
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {
        rec(() -> {
            int target = Conversion.from2Bytes(b1, b2);
            State state = state();
            boolean truthy = TNIUtils.isTrue(runtimeTop(0));
            boolean jumps = truthy == ifTrue;

            IRValue condition = builder.truthy(popValue());
            if (!truthy) condition = builder.not(condition);

            recordBranchGuard(guard(condition, state), jumps ? ip + 1 : target);
            if (jumps) onJump(target);
        });
        super.branch(b1, b2, ifTrue);
    }

    private void recordBranchGuard(GuardBool guard, int alternative) {
        branchGuards.add(guard);
        branchAlternatives.add(alternative);
    }

    @Override
    public void getIterator() {
        rec(() -> push(builder.getIterator(popValue())));
        super.getIterator();
    }

    @Override
    public void branchIterator(byte b1, byte b2) {
        rec(() -> {
            int target = Conversion.from2Bytes(b1, b2);
            TObject itrObj = runtimeTop(0);
            if (!(itrObj instanceof IteratorObject itr)) {
                abort("not an iterator");
            }
            else {
                State state = state();
                boolean hasNext = itr.hasNext();
                IRValue iterator = popValue();
                IRValue condition = builder.iterHasNext(iterator);
                if (!hasNext) condition = builder.not(condition);
                recordBranchGuard(guard(condition, state), hasNext ? target : ip + 1);
                if (hasNext) push(iterator);
                else onJump(target);
            }
        });
        super.branchIterator(b1, b2);
    }

    @Override
    public void iteratorNext() {
        rec(() -> {
            IRValue iterator = popValue();
            IRValue next = builder.iterNext(iterator);
            push(iterator);
            push(next);
        });
        super.iteratorNext();
    }

    @Override
    public void callInplace(byte argCount) {
        rec(() -> recordCall(argCount));
        super.callInplace(argCount);
    }

    private void recordCall(int argCount) {
        TObject callee = runtimeTop(0);
        State state = state();

        IRValue called = popValue();
        List<IRValue> args = new ArrayList<>(argCount);
        for (int i = 0; i < argCount; i++) args.add(popValue());

        if (isSelfTailCall(callee, argCount)) {
            // return f(...) inside of f: continue at the function entry with the new arguments
            guard(builder.isSelf(called), state);
            locals.clear();
            for (int i = argCount - 1; i >= 0; i--) push(args.get(i));
            closeLoop(entryLoop, ip);
            return;
        }

        if (!(callee instanceof Callable)) {
            abort("call of a non callable object");
            return;
        }

        IRValue call = builder.call(called, args, line);
        pendingCall = new PendingCall(call, ip + 1, frame.sp - argCount);
    }

    private boolean isSelfTailCall(TObject callee, int argCount) {
        return kind == Kind.FUNCTION
                && entryLoop != null
                && stack.isEmpty()
                && callee instanceof VirtualFunction function
                && function.getMetadata() == metadata
                && function.getOwner() == frame.getOwner()
                && function.getParameters(getCurrentThread()).count() == argCount
                && entryLoop.stackSize == argCount
                && LoopAnalysis.isReturnAt(metadata, ip + 1);
    }

    @Override
    public void returnFunction() {
        rec(() -> {
            if (allowReturn) {
                builder.returnValue(popValue());
                complete();
            }
            else {
                abort("return inside of a trace running on an interpreter frame");
            }
        });
        super.returnFunction();
    }


    /* ------------------------------------------------------------------ */
    /*  instructions that are not supported in traces                     */
    /* ------------------------------------------------------------------ */

    @Override
    public void loadNative(byte b1, byte b2) {
        unsupported("LOAD_NATIVE");
        super.loadNative(b1, b2);
    }

    @Override
    public void loadVirtual(byte b1, byte b2) {
        unsupported("LOAD_VIRTUAL");
        super.loadVirtual(b1, b2);
    }

    @Override
    public void loadType(byte b1, byte b2) {
        unsupported("LOAD_TYPE");
        super.loadType(b1, b2);
    }

    @Override
    public void buildType(byte b1, byte b2) {
        unsupported("BUILD_TYPE");
        super.buildType(b1, b2);
    }

    @Override
    public void loadExternal(byte b1, byte b2) {
        unsupported("LOAD_EXTERNAL");
        super.loadExternal(b1, b2);
    }

    @Override
    public void storeExternal(byte b1, byte b2) {
        unsupported("STORE_EXTERNAL");
        super.storeExternal(b1, b2);
    }

    @Override
    public void loadInternal(byte b1, byte b2) {
        unsupported("LOAD_INTERNAL");
        super.loadInternal(b1, b2);
    }

    @Override
    public void storeInternal(byte b1, byte b2) {
        unsupported("STORE_INTERNAL");
        super.storeInternal(b1, b2);
    }

    @Override
    public void loadSuper(byte b1, byte b2) {
        unsupported("LOAD_SUPER");
        super.loadSuper(b1, b2);
    }

    @Override
    public void storeSuper(byte b1, byte b2) {
        unsupported("STORE_SUPER");
        super.storeSuper(b1, b2);
    }

    @Override
    public void loadStatic(byte b1, byte b2) {
        unsupported("LOAD_STATIC");
        super.loadStatic(b1, b2);
    }

    @Override
    public void storeStatic(byte b1, byte b2) {
        unsupported("STORE_STATIC");
        super.storeStatic(b1, b2);
    }

    @Override
    public void loadAbstract(byte b1, byte b2) {
        unsupported("LOAD_ABSTRACT");
        super.loadAbstract(b1, b2);
    }

    @Override
    public void enterTry(byte b1, byte b2) {
        unsupported("ENTER_TRY");
        super.enterTry(b1, b2);
    }

    @Override
    public void leaveTry() {
        unsupported("LEAVE_TRY");
        super.leaveTry();
    }

    @Override
    public void throwError() {
        unsupported("THROW");
        super.throwError();
    }

    @Override
    public void callMapped(byte argCount) {
        unsupported("CALL_MAPPED");
        super.callMapped(argCount);
    }

    @Override
    public void toInplaceArgument() {
        unsupported("TO_INP_ARG");
        super.toInplaceArgument();
    }

    @Override
    public void toMappedArgument(byte b1, byte b2) {
        unsupported("TO_MAP_ARG");
        super.toMappedArgument(b1, b2);
    }

    @Override
    public void callSuper(byte argCount) {
        unsupported("CALL_SUPER");
        super.callSuper(argCount);
    }

    @Override
    public void importModule(byte b1, byte b2) {
        unsupported("IMPORT");
        super.importModule(b1, b2);
    }

    @Override
    public void useMembers() {
        unsupported("USE_MEMBERS");
        super.useMembers();
    }

    @Override
    public void use(byte b1, byte b2) {
        unsupported("USE");
        super.use(b1, b2);
    }

    @Override
    public void loadName(byte b1, byte b2) {
        unsupported("LOAD_NAME");
        super.loadName(b1, b2);
    }

    @Override
    public void setOwner() {
        unsupported("SET_OWNER");
        super.setOwner();
    }

    @Override
    public void extend(byte b1, byte b2) {
        unsupported("EXTEND");
        super.extend(b1, b2);
    }

}
