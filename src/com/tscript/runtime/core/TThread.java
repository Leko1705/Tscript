package com.tscript.runtime.core;

import com.tscript.runtime.debug.*;
import com.tscript.runtime.heap.Heap;
import com.tscript.runtime.jit.JIT;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.tni.NativePrint;
import com.tscript.runtime.type.*;
import com.tscript.tscriptc.generation.Opcode;
import com.tscript.tscriptc.util.Conversion;

import java.io.File;
import java.util.*;

public class TThread extends Thread implements Debuggable<ThreadInfo>, Interpreter {

    private final TscriptVM vm;
    private final Callable baseFunction;
    protected final ArrayDeque<Frame> frameStack = new ArrayDeque<>();
    private final int threadID;

    private int frameExitThreshold = 0;
    private Data returnValue;

    private volatile boolean running = true;

    private Interpreter interpreter = this;


    public TThread(TscriptVM vm, Callable callable, int threadID) {
        this.vm = vm;
        this.baseFunction = callable;
        this.threadID = threadID;
    }

    public int getThreadID() {
        return threadID;
    }

    @Override
    public void run() {
        begin();
    }

    public void terminate(){
        running = false;
    }

    protected void begin(){
        try {
            if (baseFunction instanceof VirtualFunction v) {
                invoke(v);
                execLoop();
            } else {
                baseFunction.call(this, List.of());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        vm.killThread(threadID);
    }

    private void execLoop(){
        while (frameStack.size() > frameExitThreshold && running)
            processNext();
    }

    private void processNext(){
        byte[] instruction = frameStack.element().fetch();
        decodeAndExecute(instruction);
    }


    private void decodeAndExecute(byte[] instruction){
        Opcode opcode = Opcode.of(instruction[0]);

        switch (opcode){
            case PUSH_NULL -> interpreter.pushNull();
            case PUSH_INT -> interpreter.pushInt(instruction[1]);
            case PUSH_BOOL -> interpreter.pushBool(instruction[1] == 1);
            case STORE_GLOBAL -> interpreter.storeGlobal(instruction[1]);
            case LOAD_GLOBAL -> interpreter.loadGlobal(instruction[1]);
            case STORE_LOCAL -> interpreter.storeLocal(instruction[1]);
            case LOAD_LOCAL -> interpreter.loadLocal(instruction[1]);
            case LOAD_CONST -> interpreter.loadConst(instruction[1]);
            case CONTAINER_READ -> interpreter.containerRead();
            case CONTAINER_WRITE -> interpreter.containerWrite();
            case RETURN -> interpreter.returnVirtual();
            case WRAP_ARGUMENT -> interpreter.wrapArgument(instruction[1]);
            case CALL -> interpreter.call(instruction[1]);
            case POP -> interpreter.pop();
            case MAKE_RANGE -> interpreter.makeRange();
            case MAKE_ARRAY -> interpreter.makeArray(instruction[1]);
            case MAKE_DICT -> interpreter.makeDict(instruction[1]);
            case ENTER_TRY -> interpreter.enterTry(instruction[1]);
            case LEAVE_TRY -> interpreter.leaveTry();
            case THROW -> interpreter.throwError();
            case GOTO -> interpreter.jumpTo(jumpAddress(instruction[1], instruction[2]));
            case GET_ITR -> interpreter.getIterator();
            case ITR_NEXT -> interpreter.iteratorNext();
            case BRANCH_ITR -> interpreter.branchIterator(jumpAddress(instruction[1], instruction[2]));
            case BRANCH_IF_FALSE -> interpreter.branchOn(false, jumpAddress(instruction[1], instruction[2]));
            case BRANCH_IF_TRUE -> interpreter.branchOn(true, jumpAddress(instruction[1], instruction[2]));
            case LOAD_MEMBER -> interpreter.loadMember(instruction[1]);
            case STORE_MEMBER -> interpreter.storeMember(instruction[1]);
            case LOAD_MEMBER_FAST -> interpreter.loadMemberFast(instruction[1]);
            case STORE_MEMBER_FAST -> interpreter.storeMemberFast(instruction[1]);
            case EQUALS -> interpreter.compare(true);
            case NOT_EQUALS -> interpreter.compare(false);
            case ADD, SUB, MUL, DIV, IDIV, MOD, POW,
                    AND, OR, XOR, LT, GT, LEQ, GEQ,
                    SLA, SRA, SRL -> interpreter.binaryOperation(opcode);
            case NOT, NEG, POS -> interpreter.unaryOperation(opcode);
            case PUSH_THIS -> interpreter.pushThis();
            case GET_TYPE -> interpreter.getType();
            case CALL_SUPER -> interpreter.callSuper(instruction[1]);
            case LOAD_ABSTRACT_IMPL -> interpreter.loadAbstractMethod(instruction[1]);
            case LOAD_STATIC -> interpreter.loadStatic(instruction[1]);
            case STORE_STATIC -> interpreter.storeStatic(instruction[1]);
            case BREAK_POINT -> interpreter.onBreakPoint();
            case USE -> interpreter.use();
            case LOAD_NAME -> interpreter.loadName(instruction[1]);
            case NEW_LINE -> interpreter.setLine(Conversion.fromBytes(
                    instruction[1],
                    instruction[2],
                    instruction[3],
                    instruction[4]));
            default ->
                    throw new IllegalStateException("invalid opcode " + opcode + " 0x" + Integer.toHexString(instruction[0]));
        }

    }

    private int jumpAddress(byte b1, byte b2){
        return ((b1 & 0xff) << 8) | (b2 & 0xff);
    }

    private Frame frame(){
        return frameStack.element();
    }


    @JITSensitive
    public Data storeGlobal(int index, Data data){
        TModule module = (TModule) unpack(frame().getModuleReference());
        return module.storeGlobal(index, data);
    }

    private Member searchStaticMember(String name){
        Frame frame = frame();
        TObject owner = unpack(frame.getOwner());
        TType type = owner.getType();
        Member member = null;
        while (member == null && type != null){
            member = type.get(name);
            type = type.getSuper();
        }
        if (member == null){
            reportRuntimeError("can not find static member '" + name + "'");
            return null;
        }
        else if (member.visibility != Visibility.PUBLIC){
            reportRuntimeError("'" + name + "' is " + member.visibility + " and can not be accessed");
            return null;
        }
        return member;
    }



    private Member accessMember(TObject accessed, String memberName){
        Member member = accessed.get(memberName);
        if (member == null) {
            String errPrefix = accessed + " has no ";
            if (accessed instanceof TType) errPrefix += "static ";
            reportRuntimeError(errPrefix + "member '" + memberName + "'");
            return null;
        }
        else if (member.visibility != Visibility.PUBLIC){
            reportRuntimeError("'" + memberName + "' is " + member.visibility + " and can not be accessed");
            return null;
        }
        return member;
    }



    private List<Argument> getCallerArguments(int argc){
        ArrayList<Argument> argList = new ArrayList<>();
        for (int i = 0; i < argc; i++){
            Data d = pop();
            if (d instanceof Argument arg){
                argList.add(arg);
            }
            else {
                argList.add(new Argument(null, d));
            }
        }
        return argList;
    }

    public void push(Data data){
        frame().push(data);
    }

    protected void invoke(VirtualFunction function){
        frameStack.push(function.buildFrame());
    }

    @JITSensitive
    public Data call(Callable callable, List<Argument> args){
        if (checkStackOverflowError()) return null;

        if (callable instanceof VirtualFunction v) {
            int prevThreshold = frameExitThreshold;
            frameExitThreshold = frameStack.size();
            v.call(this, args);
            execLoop();
            frameExitThreshold = prevThreshold;
            return returnValue;
        }
        else {
            putFrame(callable);
            Data d = callable.call(this, args);
            popFrame();
            return d;
        }
    }


    private boolean checkStackOverflowError(){
        if (frameStack.size() == 30_000){
            reportRuntimeError("stackOverflowError");
            return true;
        }
        return false;
    }

    protected void putFrame(Callable callable){
        frameStack.push(Frame.createFakeFrame(callable));
    }

    protected void popFrame(){
        if (!frameStack.isEmpty())
            frameStack.pop();
    }

    @JITSensitive
    public void reportRuntimeError(String msg){
        reportRuntimeError(new TString(msg));
    }

    @JITSensitive
    public void reportRuntimeError(Data data){
        Frame frame = frame();
        if (frame.inSafeSpot()){
            frame.escapeError();
            frame.push(data);
            return;
        }

        String msg = NativePrint.makePrintable(this, data);
        if (msg == null) return;

        StringBuilder errorLog = new StringBuilder(msg);
        errorLog.append('\n');
        do {
            frame = frameStack.pop();
            errorLog.append("in ").append(frame.getName());
            int line = frame.line();
            if (line != -1)
                errorLog.append(" (line: ")
                        .append(frame.line())
                        .append(")");
            errorLog.append('\n');
        }while (!frameStack.isEmpty() && !frameStack.element().inSafeSpot());

        if (frameStack.isEmpty()){
            System.err.println(errorLog);
            vm.killThread(threadID);
            return;
        }

        frame = frame();
        frame.escapeError();
        frame.push(data);
    }

    @JITSensitive
    public boolean isTrue(Data data){
        TObject obj = unpack(data);
        if (obj instanceof TBoolean i && !i.get()) return false;
        if (obj == TNull.NULL) return false;
        if (obj instanceof TInteger i && i.get() == 0) return false;
        if (obj instanceof TString s && s.get().isEmpty()) return false;
        return !(obj instanceof TArray a) || !a.get().isEmpty();
    }

    public TObject unpack(Data data){
        if (data.isReference()){
            Heap heap = vm.getHeap();
            return heap.load(data.asReference());
        }
        return data.asValue();
    }

    public Reference storeHeap(TObject object){
        Heap heap = vm.getHeap();
        return heap.store(object);
    }

    @JITSensitive
    public Data loadFromPool(int id){
        Frame frame = frame();
        TModule module = (TModule) unpack(frame.getModuleReference());
        Pool pool = module.getPool();
        return pool.loadData(id);
    }

    public String loadStringFromPool(int id){
        Frame frame = frame();
        TModule module = (TModule) unpack(frame.getModuleReference());
        Pool pool = module.getPool();
        return pool.loadString(id);
    }

    private void reassignValue(Data prev, Data assigned){

        Reference prevPtr = prev != null && prev.isReference() ? prev.asReference() : null;
        Reference assignPtr = assigned != null && assigned.isReference() ? assigned.asReference() : null;

        if (prevPtr == null && assignPtr == null)
            return;

        vm.gc(this, prevPtr, assignPtr);
    }

    @JITSensitive
    public void gc(){
        vm.gc(this);
    }

    protected JIT getJIT(){
        return vm.getJit();
    }
    
    public void pushNull() {
        push(TNull.NULL);
    }
    
    public void pushInt(int i) {
        push(new TInteger(i));
    }
    
    public void pushBool(boolean b) {
        push(TBoolean.of(b));
    }
    
    public void pushThis() {
        push(frame().getOwner());
    }
    
    public void storeGlobal(int address) {
        Data dataToWrite = pop();
        Data displaced = ((TModule)unpack(frame().getModuleReference())).storeGlobal(address, dataToWrite);
        reassignValue(displaced, dataToWrite);
    }

    public void loadGlobal(int address) {
        TModule module = (TModule) unpack(frame().getModuleReference());
        push(module.loadGlobal(address));
    }

    public void storeLocal(int address) {
        Data dataToWrite = pop();
        Frame frame = frame();
        Data displaced = frame.store(address, dataToWrite);
        reassignValue(displaced, dataToWrite);
    }

    public void loadLocal(int address) {
        push(frame().load(address));
    }

    public void loadConst(int address) {
        Data data = loadFromPool(address);
        if (data == null) return;
        Frame frame = frame();
        if (data instanceof Callable c){
            if (frame.getOwner() != null){
                c.setOwner(frame.getOwner());
            }
            else {
                c.setOwner(c);
            }
            if (!(c instanceof TType))
                data = storeHeap(c);
        }
        push(data);
    }

    public void containerRead() {
        Data candidate = pop();
        if (!(candidate instanceof ContainerAccessible accessible)){
            reportRuntimeError(candidate + " is not accessible");
            return;
        }
        Data key = pop();
        Data accessed = accessible.readFromContainer(this, key);
        if (accessed == null)
            return;
        push(accessed);
    }

    public void containerWrite() {
        Data candidate = pop();
        if (!(candidate instanceof ContainerWriteable writeable)){
            reportRuntimeError(candidate + " is not accessible");
            return;
        }
        Data key = pop();
        Data value = pop();
        writeable.writeToContainer(this, key, value);
    }

    public void returnVirtual() {
        returnValue = pop();
        frameStack.pop();
        if (frameStack.size() <= frameExitThreshold) return;
        push(returnValue);
    }

    public void wrapArgument(int utf8Address) {
        String refName = loadStringFromPool(utf8Address);
        push(new Argument(refName, pop()));
    }

    public void call(int argc) {
        if (checkStackOverflowError())
            return;

        TObject called = unpack(pop());
        if (!(called instanceof Callable c)) {
            reportRuntimeError(called + " is not callable");
            return;
        }

        List<Argument> argList = getCallerArguments(argc);
        Data res = c.call(this, argList);
        if (res != null && frameStack.size() > frameExitThreshold)
            push(res);
    }

    public Data pop() {
        return frame().pop();
    }

    public void makeRange() {
        Data to = unpack(pop());
        Data from = unpack(pop());

        if (!(from instanceof TInteger f)) {
            reportRuntimeError("can not build range from " + from);
            return;
        }

        if (!(to instanceof TInteger t)) {
            reportRuntimeError("can not build range from " + to);
            return;
        }

        TRange range = new TRange(f, t);
        push(range);
    }

    public void makeArray(int cnt) {
        TArray array = new TArray();
        List<Data> content = array.get();
        for (; cnt > 0; cnt--)
            content.add(pop());
        push(array);
    }

    public void makeDict(int cnt) {
        TDictionary dict = new TDictionary();
        Map<Data, Data> content = dict.get();
        for (; cnt > 0; cnt--){
            Data key = pop();
            Data value = pop();
            content.put(key, value);
        }
        push(dict);
    }

    public void enterTry(int safeAddress) {
        frame().enterSafeSpot(safeAddress);
    }

    public void leaveTry() {
        frame().leaveSafeSpot();
    }

    public void throwError() {
        reportRuntimeError(pop());
    }

    public void jumpTo(int address) {
        frame().jumpTo(address);
    }

    public void getIterator() {
        Data candidate = pop();
        if (!(candidate instanceof IterableObject iterable)){
            reportRuntimeError(candidate + " is not iterable");
            return;
        }
        push(iterable.iterator());
    }

    public void iteratorNext() {
        IteratorObject itr = (IteratorObject) pop();
        push(itr);
        push(itr.next());
    }

    public void branchIterator(int address) {
        IteratorObject itr = (IteratorObject) pop();
        if (!itr.hasNext()) {
            jumpTo(address);
            return;
        }
        push(itr);
    }

    public void branchOn(boolean when, int address) {
        Data data = pop();
        if (isTrue(data) == when){
            jumpTo(address);
        }
    }

    public void loadMember(int utf8Address) {
        String memberName = loadStringFromPool(utf8Address);
        Member member = accessMember(unpack(pop()), memberName);
        if (member == null) return;
        push(member.data);
    }

    public void storeMember(int utf8Address) {
        String memberName = loadStringFromPool(utf8Address);
        Data accessed = pop();
        Data dataToWrite = pop();
        Member member = accessMember(unpack(accessed), memberName);
        if (member == null) return;
        if (member.kind == Member.Kind.IMMUTABLE){
            reportRuntimeError("can not assign to a constant");
            return;
        }
        member.data = dataToWrite;
    }

    public void loadMemberFast(int address) {
        Frame frame = frame();
        TObject owner = unpack(frame.getOwner());
        Member member = owner.get(address);
        push(member.data);
    }

    public void storeMemberFast(int address) {
        Frame frame = frame();

        TObject owner = unpack(frame.getOwner());
        Member member = owner.get(address);
        Data assigned  = pop();
        if (member.kind == null && member.data == null){
            // member was not initialized yet
            member.kind = unpack(assigned) instanceof Callable
                    ? Member.Kind.IMMUTABLE
                    : Member.Kind.MUTABLE;
        }
        member.data = assigned;
    }

    public void compare(boolean onTrue) {
        boolean b = pop().equals(pop());
        push(TBoolean.of(b == onTrue));
    }

    public void binaryOperation(Opcode operation) {
        Data right = pop();
        Data left = pop();
        Data result = ALU.performBinaryOperation(left, right, operation, this);
        if (result != null)
            push(result);
    }

    public void unaryOperation(Opcode operation) {
        Data operand = pop();
        Data result = ALU.performUnaryOperation(operand, operation, this);
        if (result != null)
            push(result);
    }

    public void getType() {
        push((unpack(pop())).getType());
    }

    public void callSuper(int argc) {
        Frame frame = frame();
        TObject owner = unpack(frame.getOwner());
        TType type = owner.getType();
        TType superType = type.getSuper();
        Callable superConstructor = superType.getConstructor();
        superConstructor.setOwner(frame.getOwner());
        List<Argument> argList = getCallerArguments(argc);
        superConstructor.call(this, argList);
    }

    public void loadAbstractMethod(int utf8Address) {
        String methodName = loadStringFromPool(utf8Address);
        Frame frame = frame();
        TObject owner = unpack(frame.getOwner());
        TType currType = owner.getType();
        while (currType != null){
            Member member = owner.get(methodName);
            if (member == null) {
                currType = currType.getSuper();
                continue;
            }
            push(member.data);
            return;
        }
        reportRuntimeError("can not find implementation of '" + methodName + "'");
    }

    public void loadStatic(int utf8Address) {
        String memberName = loadStringFromPool(utf8Address);
        Member member = searchStaticMember(memberName);
        if (member == null) return;
        push(member.data);
    }

    public void storeStatic(int utf8Address) {
        Data dataToWrite = pop();
        String memberName = loadStringFromPool(utf8Address);
        Member member = searchStaticMember(memberName);
        if (member == null) return;
        if (member.kind == Member.Kind.IMMUTABLE){
            reportRuntimeError("can not assign to a constant");
            return;
        }
        member.data = dataToWrite;
    }

    public void onBreakPoint() {

        final Interpreter currentInterpreter = interpreter;

        interpreter = new DebugInterpreter(currentInterpreter, () -> {
            DebugAction action = vm.debug(TThread.this);
            switch (action){
                case STEP -> { /* simply run until next halt */ }
                case RESUME -> interpreter = currentInterpreter;
                case QUIT -> vm.quit();
                default -> throw new IllegalStateException("unsupported DebugAction: " + action);
            }
        });

    }

    public void use() {
        TObject object = unpack(pop());
        Frame frame = frame();
        for (Member member : object.getMembers()){
            boolean success = frame.storeName(member.name, member.data);
            if (!success){
                reportRuntimeError("name '" + member.name + "' already used");
                return;
            }
        }
    }

    public void loadName(byte b) {
        String name = loadStringFromPool(b);
        Frame frame = frame();
        Data data = frame.loadName(name);
        if (data == null){
            reportRuntimeError("can not find name '" + name + "'");
            return;
        }
        push(data);
    }

    @JITSensitive
    public void setLine(int line){
        frame().setLine(line);
    }


    @Override
    public ThreadInfo loadInfo(Heap heap) {
        return new ThreadInfoImpl(heap);
    }


    private class ThreadInfoImpl implements ThreadInfo {

        private final List<FrameInfo> frameTrees;

        public ThreadInfoImpl(Heap heap){
            frameTrees = new ArrayList<>();
            for (Frame frame : frameStack)
                frameTrees.add(frame.loadInfo(heap));
        }

        @Override
        public int getID() {
            return threadID;
        }

        @Override
        public int getLine() {
            return frame().line();
        }

        @Override
        public List<FrameInfo> getFrameTrees() {
            return frameTrees;
        }
    }

}
