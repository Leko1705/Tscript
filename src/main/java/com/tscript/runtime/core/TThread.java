package com.tscript.runtime.core;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TThread extends Thread implements Environment, TObject {

    public static final Type TYPE = Type.newBuilder("Thread")
            .setAbstract(true)
            .setConstructor(((env, params) -> {
                throw new AbstractMethodError();
            }))
            .build();

    private final TscriptVM vm;
    private final Callable baseFunction;
    private final List<TObject> arguments;

    public final ArrayDeque<Frame> frameStack = new ArrayDeque<>();
    private volatile Interpreter interpreter;
    private volatile boolean halt = false;

    protected volatile boolean running = true;

    protected TObject returnValue;
    protected int frameStackExitThreshold = 0;

    private final Map<String, Member> members = new HashMap<>(Map.of(
            "name", Member.of(Visibility.PUBLIC, false, "name", new NameMethod()),
            "id", Member.of(Visibility.PUBLIC, false, "id", new IdMethod()),
            "start", Member.of(Visibility.PUBLIC, false, "start", new StartMethod()),
            "stop", Member.of(Visibility.PUBLIC, false, "stop", new StopMethod())
    ));

    public TThread(TscriptVM vm, Callable callable, List<TObject> args, Function<TThread, Interpreter> interpreterSupplier) {
        this.vm = vm;
        this.baseFunction = callable;
        this.arguments = args;

        interpreter = interpreterSupplier.apply(this);
    }

    @Override
    public String getDisplayName() {
        String threadName = Thread.currentThread().getName();
        if (threadName.equals("main"))
            threadName = "Main-Thread";
        return threadName;
    }

    @Override
    public void run() {
        begin();
    }

    public boolean isRunning() {
        return running;
    }

    protected void begin() {
        try {
            invokeFromUnknownContext(baseFunction, arguments);
        }
        catch (Exception e){
            String s = "An Internal Error occurred";
            if (!frameStack.isEmpty()){

                Frame frame = getFrame();

                if (frame.getMetadata().isVirtual()) {
                    VirtualFrame vframe = (VirtualFrame) frame;
                    s += " near line " + vframe.line();
                    s += " in module: " + vframe.getModule().getCanonicalPath();
                }
                else {
                    s += "in native imageKey";
                }

            }
            else {
                System.out.println(" (frame is empty)");
            }
            System.err.println(s);
            e.printStackTrace(System.err);
        }
        vm.removeThread(getId());
    }

    private TObject invokeFromUnknownContext(Callable callable, List<TObject> args) {
        int prev = frameStackExitThreshold;
        frameStackExitThreshold = frameStack.size();
        TObject result;

        if (callable.isVirtual()){
            callable.call(this, args);
            startExecutionCycle();
            result = returnValue;
        }
        else {
            result = callable.call(this, args);
        }

        frameStackExitThreshold = prev;
        returnValue = result;
        return result;
    }

    /**
     * Pushes the given frame and runs it (and all frames it calls) to completion.
     * Used to continue execution in the interpreter after compiled code deoptimized.
     * @return the return value of the frame. Only valid if the frame stack has its original size afterwards
     */
    public TObject runFrame(VirtualFrame frame) {
        int prev = frameStackExitThreshold;
        frameStackExitThreshold = frameStack.size();
        frameStack.push(frame);
        startExecutionCycle();
        frameStackExitThreshold = prev;
        return returnValue;
    }

    /**
     * The current nesting depth of native images (compiled code) running on this thread.
     */
    public int jitDepth = 0;

    /*
     * Shadow frames: compiled code of a function runs without an interpreter frame. While it
     * calls out (e.g. into another function), it registers a shadow frame, so that error
     * messages still list the compiled function. A shadow frame sits logically on top of the
     * first `depth` frames of the frame stack.
     */
    private VirtualFunction.VirtualMetadata[] shadowMetadata = new VirtualFunction.VirtualMetadata[16];
    private int[] shadowLines = new int[16];
    private int[] shadowDepths = new int[16];
    private int shadowSize = 0;

    /**
     * Registers a shadow frame for compiled code of the given function.
     * @return a mark to pass to {@link #popShadowFrames(int)}
     */
    public int pushShadowFrame(VirtualFunction.VirtualMetadata metadata, int line) {
        int mark = shadowSize;
        if (shadowSize == shadowMetadata.length) {
            shadowMetadata = Arrays.copyOf(shadowMetadata, shadowSize * 2);
            shadowLines = Arrays.copyOf(shadowLines, shadowSize * 2);
            shadowDepths = Arrays.copyOf(shadowDepths, shadowSize * 2);
        }
        shadowMetadata[shadowSize] = metadata;
        shadowLines[shadowSize] = line;
        shadowDepths[shadowSize] = frameStack.size();
        shadowSize++;
        return mark;
    }

    public int shadowFrameCount() {
        return shadowSize;
    }

    public void popShadowFrames(int mark) {
        while (shadowSize > mark) {
            shadowMetadata[--shadowSize] = null;
        }
    }

    private void appendShadowFrames(StringBuilder errorLog, int depth) {
        while (shadowSize > 0 && shadowDepths[shadowSize - 1] >= depth) {
            VirtualFunction.VirtualMetadata metadata = shadowMetadata[--shadowSize];
            shadowMetadata[shadowSize] = null;
            errorLog.append("in ").append(metadata.name()).append(" (");
            errorLog.append("line: ").append(shadowLines[shadowSize]).append("; ");
            errorLog.append("module: ").append(metadata.module().getCanonicalPath()).append(")\n");
        }
    }

    private void discardShadowFrames(int depth) {
        while (shadowSize > 0 && shadowDepths[shadowSize - 1] >= depth) {
            shadowMetadata[--shadowSize] = null;
        }
    }

    private void startExecutionCycle(){
        while (running && frameStack.size() > frameStackExitThreshold){
            if (halt) performHalt();
            byte[] instruction = topVirtualFrame().fetch();
            decodeAndExecute(instruction);
        }
    }

    public void halt() {
        halt = true;
    }

    public void checkHalt(){
        if (!halt) return;
        if (this == Thread.currentThread()) return;
        performHalt();
    }

    private synchronized void performHalt(){
        try {
            wait();
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public synchronized void release(){
        halt = false;
        this.notify();
    }

    private void decodeAndExecute(byte[] instruction){
        Opcode opcode = Opcode.of(instruction[0]);
        switch (opcode){
            case PUSH_NULL -> interpreter.pushNull();
            case PUSH_INT -> interpreter.pushInt(instruction[1]);
            case PUSH_BOOL -> interpreter.pushBool(instruction[1]);
            case LOAD_CONST -> interpreter.loadConst(instruction[1], instruction[2]);
            case PUSH_THIS -> interpreter.pushThis();
            case LOAD_NATIVE -> interpreter.loadNative(instruction[1], instruction[2]);
            case LOAD_VIRTUAL -> interpreter.loadVirtual(instruction[1], instruction[2]);
            case LOAD_BUILTIN -> interpreter.loadBuiltin(instruction[1], instruction[2]);
            case SET_OWNER -> interpreter.setOwner();
            case LOAD_TYPE -> interpreter.loadType(instruction[1], instruction[2]);
            case BUILD_TYPE -> interpreter.buildType(instruction[1], instruction[2]);
            case POP -> interpreter.pop();
            case NEW_LINE -> interpreter.newLine(Conversion.fromBytes(instruction[1], instruction[2], instruction[3], instruction[4]));
            case LOAD_GLOBAL -> interpreter.loadGlobal(instruction[1]);
            case STORE_GLOBAL -> interpreter.storeGlobal(instruction[1]);
            case LOAD_LOCAL -> interpreter.loadLocal(instruction[1]);
            case STORE_LOCAL -> interpreter.storeLocal(instruction[1]);
            case LOAD_EXTERNAL -> interpreter.loadExternal(instruction[1], instruction[2]);
            case STORE_EXTERNAL -> interpreter.storeExternal(instruction[1], instruction[2]);
            case LOAD_INTERNAL -> interpreter.loadInternal(instruction[1], instruction[2]);
            case STORE_INTERNAL -> interpreter.storeInternal(instruction[1], instruction[2]);
            case LOAD_SUPER -> interpreter.loadSuper(instruction[1], instruction[2]);
            case STORE_SUPER -> interpreter.storeSuper(instruction[1], instruction[2]);
            case LOAD_STATIC -> interpreter.loadStatic(instruction[1], instruction[2]);
            case STORE_STATIC -> interpreter.storeStatic(instruction[1], instruction[2]);
            case CONTAINER_READ -> interpreter.containerRead();
            case CONTAINER_WRITE -> interpreter.containerWrite();
            case LOAD_ABSTRACT -> interpreter.loadAbstract(instruction[1], instruction[2]);
            case ADD, SUB, MUL, DIV, IDIV, MOD, POW,
                 AND, OR, XOR,
                 LT, GT, LEQ, GEQ,
                 SHL, SHR, USHR -> interpreter.binaryOperation(opcode);
            case EQUALS -> interpreter.equals(true);
            case NOT_EQUALS -> interpreter.equals(false);
            case NOT -> interpreter.not();
            case NEG -> interpreter.negate();
            case POS -> interpreter.posivate();
            case GET_TYPE -> interpreter.getType();
            case MAKE_ARRAY -> interpreter.makeArray(instruction[1]);
            case MAKE_DICT -> interpreter.makeDict(instruction[1]);
            case MAKE_RANGE -> interpreter.makeRange();
            case GOTO -> interpreter.jumpTo(instruction[1], instruction[2]);
            case BRANCH_IF_TRUE -> interpreter.branch(instruction[1], instruction[2], true);
            case BRANCH_IF_FALSE -> interpreter.branch(instruction[1], instruction[2], false);
            case GET_ITR -> interpreter.getIterator();
            case BRANCH_ITR -> interpreter.branchIterator(instruction[1], instruction[2]);
            case ITR_NEXT -> interpreter.iteratorNext();
            case ENTER_TRY -> interpreter.enterTry(instruction[1], instruction[2]);
            case LEAVE_TRY -> interpreter.leaveTry();
            case THROW -> interpreter.throwError();
            case CALL_INPLACE -> interpreter.callInplace(instruction[1]);
            case CALL_MAPPED -> interpreter.callMapped(instruction[1]);
            case TO_INP_ARG -> interpreter.toInplaceArgument();
            case TO_MAP_ARG -> interpreter.toMappedArgument(instruction[1], instruction[2]);
            case RETURN -> interpreter.returnFunction();
            case CALL_SUPER -> interpreter.callSuper(instruction[1]);
            case IMPORT -> interpreter.importModule(instruction[1], instruction[2]);
            case USE -> interpreter.use(instruction[1], instruction[2]);
            case USE_MEMBERS -> interpreter.useMembers();
            case LOAD_NAME -> interpreter.loadName(instruction[1], instruction[2]);
            case DUP -> interpreter.dup();
            case EXTEND -> interpreter.extend(instruction[1], instruction[2]);
            default -> throw new ExecutionException("unsupported opcode: " + opcode);
        }
    }

    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    protected void push(TObject object){
        topVirtualFrame().push(object);
    }

    protected TObject pop(){
        return topVirtualFrame().pop();
    }

    public Frame getFrame(){
        return frameStack.element();
    }

    public void reportRuntimeError(TObject object) {
        VirtualFrame vframe = topVirtualFrame();
        if (vframe.inUnsafeSpot()){
            discardShadowFrames(frameStack.size());
            vframe.escapeError();
            vframe.push(object);
            return;
        }

        String msg = TNIUtils.toString(this, object);
        if (msg == null) return;

        String threadName = Thread.currentThread().getName();
        if (threadName.equals("main"))
            threadName = "Main-Thread";
        StringBuilder errorLog = new StringBuilder("error in ").append(threadName).append(": ");
        errorLog.append(msg).append('\n');
        do {
            appendShadowFrames(errorLog, frameStack.size());
            Frame frame = frameStack.remove();
            errorLog.append("in ").append(frame.getMetadata().name());

            if (frame.getMetadata().isVirtual()) {
                vframe = (VirtualFrame) frame;
                errorLog.append(" (");
                errorLog.append("line: ").append(vframe.line()).append("; ");
                errorLog.append("module: ").append(vframe.getModule().getCanonicalPath()).append(")");
            }
            else {
                errorLog.append(" (native)");
            }
            errorLog.append('\n');
        }while (!frameStack.isEmpty() && !topVirtualFrame().inUnsafeSpot());

        appendShadowFrames(errorLog, frameStack.size());

        if (frameStack.isEmpty()){
            System.err.println(errorLog);
            running = false;
            return;
        }

        vframe = topVirtualFrame();
        vframe.escapeError();
        vframe.push(object);
    }

    @Override
    public TThread getCurrentThread() {
        return this;
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {
        return interpreter.call(called, arguments);
    }

    protected TObject call0(Callable called, List<TObject> arguments) {
        return invokeFromUnknownContext(called, arguments);
    }

    public TscriptVM getVM() {
        return vm;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isMainThread(){
        return Thread.currentThread().getName().equals("main");
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public Iterable<Member> getMembers() {
        return members.values();
    }

    @Override
    public Member loadMember(String name) {
        return members.get(name);
    }

    public VirtualFrame topVirtualFrame() {
        return (VirtualFrame) getFrame();
    }


    private class NameMethod extends NativeFunction {

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return new TString(TThread.this.getName());
        }
    }

    private class IdMethod extends NativeFunction {

        @Override
        public String getName() {
            return "id";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return new TReal((double)TThread.this.getId());
        }
    }

    private class StartMethod extends NativeFunction {

        @Override
        public String getName() {
            return "start";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            TThread.this.start();
            return Null.INSTANCE;
        }
    }


    private class StopMethod extends NativeFunction {

        @Override
        public String getName() {
            return "stop";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            TThread.this.running = false;
            return Null.INSTANCE;
        }
    }
}
