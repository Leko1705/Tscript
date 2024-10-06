package com.tscript.runtime.core;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TThread extends Thread implements Environment, TObject {

    public static final Type TYPE = new Type.Builder("Thread")
            .setAbstract(true)
            .setConstructor(((thread, params) -> {
                throw new AbstractMethodError();
            }))
            .build();

    private final TscriptVM vm;
    private final Callable baseFunction;
    private final List<TObject> arguments;

    public final ArrayDeque<Frame> frameStack = new ArrayDeque<>();
    private volatile Interpreter interpreter;

    protected volatile boolean running = true;

    protected TObject returnValue;
    protected int frameStackExitThreshold = 0;

    private final Map<String, Member> members = new HashMap<>(Map.of(
            "name", new Member(Visibility.PUBLIC, false, "name", new NameMethod()),
            "id", new Member(Visibility.PUBLIC, false, "id", new IdMethod()),
            "start", new Member(Visibility.PUBLIC, false, "start", new StartMethod()),
            "stop", new Member(Visibility.PUBLIC, false, "stop", new StopMethod())
    ));

    public TThread(TscriptVM vm, Callable callable, List<TObject> args) {
        this.vm = vm;
        this.baseFunction = callable;
        this.arguments = args;

        interpreter = new BaseInterpreter(this);
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
            if (frameStack.element().line() != -1){
                s += " near line " + frameStack.element().line();
            }
            s += " in module: " + frameStack.element().getModule().getCanonicalPath();
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
            frameStack.push(Frame.createNativeFrame(callable));
            result = callable.call(this, args);
            if (!frameStack.isEmpty()) frameStack.pop();
        }

        frameStackExitThreshold = prev;
        returnValue = result;
        return result;
    }

    private void startExecutionCycle(){
        while (running && frameStack.size() > frameStackExitThreshold){
            byte[] instruction = frameStack.element().fetch();
            decodeAndExecute(instruction);
        }
    }

    private void decodeAndExecute(byte[] instruction){
        Opcode opcode = Opcode.of(instruction[0]);
        switch (opcode){
            case PUSH_NULL -> interpreter.pushNull();
            case PUSH_INT -> interpreter.pushInt(instruction[1]);
            case PUSH_BOOL -> interpreter.pushBool(instruction[1]);
            case LOAD_CONST -> interpreter.loadConst(instruction[1], instruction[2]);
            case LOAD_TYPE -> interpreter.loadType(instruction[1], instruction[2]);
            case PUSH_THIS -> interpreter.pushThis();
            case LOAD_NATIVE -> interpreter.loadNative(instruction[1], instruction[2]);
            case LOAD_VIRTUAL -> interpreter.loadVirtual(instruction[1], instruction[2]);
            case LOAD_BUILTIN -> interpreter.loadBuiltin(instruction[1], instruction[2]);
            case SET_OWNER -> interpreter.setOwner();
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
            case LOAD_STATIC -> interpreter.loadStatic(instruction[1], instruction[2]);
            case STORE_STATIC -> interpreter.storeStatic(instruction[1], instruction[2]);
            case CONTAINER_READ -> interpreter.containerRead();
            case CONTAINER_WRITE -> interpreter.containerWrite();
            case LOAD_ABSTRACT -> interpreter.loadAbstract(instruction[1], instruction[2]);
            case ADD, SUB, MUL, DIV, IDIV, MOD, POW,
                 AND, OR, XOR,
                 LT, GT, LEQ, GEQ,
                 SLA, SRA, SRL -> interpreter.binaryOperation(opcode);
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
            case USE -> interpreter.use();
            case LOAD_NAME -> interpreter.loadName(instruction[1], instruction[2]);
            case DUP -> interpreter.dup();
            default -> throw new ExecutionException("unsupported opcode: " + opcode);
        }
    }

    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    protected void push(TObject object){
        frameStack.element().push(object);
    }

    protected TObject pop(){
        return frameStack.element().pop();
    }

    protected Frame getFrame(){
        return frameStack.element();
    }

    public void reportRuntimeError(TObject object) {
        Frame frame = getFrame();
        if (frame.inSafeSpot()){
            frame.escapeError();
            frame.push(object);
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
            frame = frameStack.pop();
            errorLog.append("in ").append(frame.getName());
            int line = frame.line();
            if (line != -1) {
                errorLog.append(" (");
                errorLog.append("line: ").append(frame.line()).append("; ");
                errorLog.append("module: ").append(frame.getModule().getCanonicalPath()).append(")");
            }
            else {
                errorLog.append(" (native)");
            }
            errorLog.append('\n');
        }while (!frameStack.isEmpty() && !frameStack.element().inSafeSpot());

        if (frameStack.isEmpty()){
            System.err.println(errorLog);
            running = false;
            return;
        }

        frame = getFrame();
        frame.escapeError();
        frame.push(object);
    }

    @Override
    public TThread getCurrentThread() {
        return this;
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {
        return invokeFromUnknownContext(called, arguments);
    }

    public TscriptVM getVM() {
        return vm;
    }

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
