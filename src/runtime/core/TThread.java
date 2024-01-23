package runtime.core;

import runtime.debug.*;
import runtime.heap.Heap;
import runtime.jit.JIT;
import runtime.jit.JITSensitive;
import runtime.tni.NativePrint;
import runtime.type.*;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.*;

public class TThread extends Thread implements Debuggable<ThreadInfo> {

    private final TscriptVM vm;
    private final Callable baseFunction;
    private final ArrayDeque<Frame> frameStack = new ArrayDeque<>();
    private final int threadID;

    private int frameExitThreshold = 0;
    private Data returnValue;

    private boolean running = true;


    private final Interpreter DEFAULT_INTERPRETER = new SimpleInterpreter();
    private Interpreter interpreter = DEFAULT_INTERPRETER;


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
            case NEW_LINE -> setLine(Conversion.fromBytes(
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

    private void storeGlobal(int addr){
        Data dataToWrite = pop();
        Data displaced = vm.storeGlobal(addr, dataToWrite);
        reassignValue(displaced, dataToWrite);
    }

    private void storeLocal(int addr){
        Data dataToWrite = pop();
        Frame frame = frame();
        Data displaced = frame.store(addr, dataToWrite);
        reassignValue(displaced, dataToWrite);
    }

    private void loadConst(int poolAddr){
        Data data = (Data) loadFromPool(poolAddr);
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

    private void operateBinary(Opcode operation){
        Data right = pop();
        Data left = pop();
        Data result = ALU.performBinaryOperation(left, right, operation, this);
        if (result != null)
            push(result);
    }

    private void operateUnary(Opcode operation){
        Data operand = pop();
        Data result = ALU.performUnaryOperation(operand, operation, this);
        if (result != null)
            push(result);
    }

    private void loadStatic(int poolAddr){
        String memberName = (String) loadFromPool(poolAddr);
        Member member = searchStaticMember(memberName);
        if (member == null) return;
        push(member.data);
    }

    private void storeStatic(int poolAddr){
        Data dataToWrite = pop();
        String memberName = (String) loadFromPool(poolAddr);
        Member member = searchStaticMember(memberName);
        if (member == null) return;
        if (member.kind == Member.Kind.IMMUTABLE){
            reportRuntimeError("can not assign to a constant");
            return;
        }
        member.data = dataToWrite;
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

    private void unsafeMemberAccess(int poolAddr){
        String memberName = (String) loadFromPool(poolAddr);
        Member member = accessMember(unpack(pop()), memberName);
        if (member == null) return;
        push(member.data);
    }

    private void unsafeMemberWrite(int poolAddr){
        String memberName = (String) loadFromPool(poolAddr);
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

    private void fastMemberAccess(int addr){
        Frame frame = frame();
        TObject owner = unpack(frame.getOwner());
        Member member = owner.get(addr);
        push(member.data);
    }

    private void fastMemberWrite(int addr){
        Frame frame = frame();

        TObject owner = unpack(frame.getOwner());
        Member member = owner.get(addr);
        Data assigned  = pop();
        if (member.kind == null && member.data == null){
            // member was not initialized yet
            member.kind = unpack(assigned) instanceof Callable
                    ? Member.Kind.IMMUTABLE
                    : Member.Kind.MUTABLE;
        }
        member.data = assigned;
    }

    private void loadAbstractMethod(int poolAddr){
        String methodName = (String) loadFromPool(poolAddr);
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
    private void jumpTo(int addr){
        frame().jumpTo(addr);
    }

    private void containerRead(){
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

    private void containerWrite(){
        Data candidate = pop();
        if (!(candidate instanceof ContainerWriteable writeable)){
            reportRuntimeError(candidate + " is not accessible");
            return;
        }
        Data key = pop();
        Data value = pop();
        writeable.writeToContainer(this, key, value);
    }

    private void getItr(){
        Data candidate = pop();
        if (!(candidate instanceof IterableObject iterable)){
            reportRuntimeError(candidate + " is not iterable");
            return;
        }
        push(iterable.iterator());
    }

    private void itrNext(){
        IteratorObject itr = (IteratorObject) pop();
        push(itr);
        push(itr.next());
    }

    private void branchItr(int addr){
        IteratorObject itr = (IteratorObject) pop();
        if (!itr.hasNext()) {
            jumpTo(addr);
            return;
        }
        push(itr);
    }

    private void branchOnBoolean(boolean when, int addr){
        Data data = pop();
        if (isTrue(data) == when){
            jumpTo(addr);
        }
    }

    private void makeRange(){
        Data to = pop();
        Data from = pop();

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

    private void makeArray(int count){
        TArray array = new TArray();
        List<Data> content = array.get();
        for (; count > 0; count--)
            content.add(pop());
        push(array);
    }

    private void makeDict(int count){
        TDictionary dict = new TDictionary();
        Map<Data, Data> content = dict.get();
        for (; count > 0; count--){
            Data key = pop();
            Data value = pop();
            content.put(key, value);
        }
        push(dict);
    }

    private void wrapArgument(int poolAddr){
        String refName = (String) loadFromPool(poolAddr);
        push(new Argument(refName, pop()));
    }

    private void call(int argc){

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

    private void callSuperConstructor(int argc){
        Frame frame = frame();
        TObject owner = unpack(frame.getOwner());
        TType type = owner.getType();
        TType superType = type.getSuper();
        Callable superConstructor = superType.getConstructor();
        superConstructor.setOwner(frame.getOwner());
        List<Argument> argList = getCallerArguments(argc);
        superConstructor.call(this, argList);
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

    private void returnVirtual(){
        returnValue = pop();
        frameStack.pop();
        if (frameStack.size() <= frameExitThreshold) return;
        push(returnValue);
    }

    protected void push(Data data){
        frame().push(data);
    }

    private Data pop(){
        return frame().pop();
    }

    protected void invoke(VirtualFunction function){
        frameStack.push(function.buildFrame());
    }

    @JITSensitive
    public Data call(Callable callable, List<Argument> args){
        if (checkStackOverflowError()) return null;
        return callFromNativeContext(callable, args);
    }

    @JITSensitive
    public void setLine(int line){
        frame().setLine(line);
    }

    private boolean checkStackOverflowError(){
        if (frameStack.size() == 30_000){
            reportRuntimeError("stackOverflowError");
            return true;
        }
        return false;
    }

    private Data callFromNativeContext(Callable callable, List<Argument> args){
        if (callable instanceof VirtualFunction v) {
            return evalNativeCalledVirtualFunction(v, args);
        }
        else {
            putFrame(callable);
            Data d = callable.call(this, args);
            popFrame();
            return d;
        }
    }

    protected void putFrame(Callable callable){
        frameStack.push(Frame.createFakeFrame(callable));
    }

    protected void popFrame(){
        if (!frameStack.isEmpty())
            frameStack.pop();
    }

    private Data evalNativeCalledVirtualFunction(VirtualFunction v, List<Argument> args){
        int prevThreshold = frameExitThreshold;
        frameExitThreshold = frameStack.size();
        v.call(this, args);
        execLoop();
        frameExitThreshold = prevThreshold;
        return returnValue;
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

        StringBuilder errorLog = new StringBuilder(NativePrint.makePrintable(this, data));
        errorLog.append('\n');
        do {
            frame = frameStack.pop();
            errorLog.append("in ").append(frame.getName())
                    .append(" (line: ").append(frame.line())
                    .append(")").append('\n');
        }while (!frameStack.isEmpty() && !frameStack.element().inSafeSpot());

        if (frameStack.isEmpty()){
            System.err.println(errorLog);
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
    public Object loadFromPool(int id){
        Frame frame = frame();
        Pool pool = frame.getPool();
        return pool.load(id, this);
    }

    private void reassignValue(Data prev, Data assigned){

        Reference prevPtr = prev != null && prev.isReference() ? prev.asReference() : null;
        Reference assignPtr = assigned != null && assigned.isReference() ? assigned.asReference() : null;

        if (prevPtr == null && assignPtr == null)
            return;

        Set<Reference> roots = vm.getRootPointers();
        roots.remove(prevPtr);
        if (assignPtr != null)
            roots.add(assignPtr);

        vm.gc(this, prevPtr, assignPtr);
    }

    @JITSensitive
    public void gc(){
        vm.gc(this);
    }

    protected JIT getJIT(){
        return vm.getJit();
    }

    @JITSensitive
    public Data loadGlobal(int index){
        return vm.loadGlobal(index);
    }

    @JITSensitive
    public Data storeGlobal(int index, Data data){
        return vm.storeGlobal(index, data);
    }

    public Set<Reference> getRootPointers(){
        return vm.getRootPointers();
    }

    private interface Interpreter {
        void pushNull();
        void pushInt(int i);
        void pushBool(boolean b);
        void pushThis();
        void storeGlobal(int address);
        void loadGlobal(int address);
        void storeLocal(int address);
        void loadLocal(int address);
        void loadConst(int address);
        void containerRead();
        void containerWrite();
        void returnVirtual();
        void wrapArgument(int utf8Address);
        void call(int argc);
        void pop();
        void makeRange();
        void makeArray(int cnt);
        void makeDict(int cnt);
        void enterTry(int safeAddress);
        void leaveTry();
        void throwError();
        void jumpTo(int address);
        void getIterator();
        void iteratorNext();
        void branchIterator(int address);
        void branchOn(boolean when, int address);
        void loadMember(int utf8Address);
        void storeMember(int utf8Address);
        void loadMemberFast(int address);
        void storeMemberFast(int address);
        void compare(boolean onTrue);
        void binaryOperation(Opcode operation);
        void unaryOperation(Opcode operation);
        void getType();
        void callSuper(int argc);
        void loadAbstractMethod(int utf8Address);
        void loadStatic(int utf8Address);
        void storeStatic(int utf8Address);
        void onBreakPoint();
    }
    private class SimpleInterpreter implements Interpreter {
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
            TThread.this.storeGlobal(address);
        }
        public void loadGlobal(int address) {
            push(vm.loadGlobal(address));
        }
        public void storeLocal(int address) {
            TThread.this.storeLocal(address);
        }
        public void loadLocal(int address) {
            push(frame().load(address));
        }
        public void loadConst(int address) {
            TThread.this.loadConst(address);
        }
        public void containerRead() {
            TThread.this.containerRead();
        }
        public void containerWrite() {
            TThread.this.containerWrite();
        }
        public void returnVirtual() {
            TThread.this.returnVirtual();
        }
        public void wrapArgument(int utf8Address) {
            TThread.this.wrapArgument(utf8Address);
        }
        public void call(int argc) {
            TThread.this.call(argc);
        }
        public void pop() {
            TThread.this.pop();
        }
        public void makeRange() {
            TThread.this.makeRange();
        }
        public void makeArray(int cnt) {
            TThread.this.makeArray(cnt);
        }
        public void makeDict(int cnt) {
            TThread.this.makeDict(cnt);
        }
        public void enterTry(int safeAddress) {
            frame().enterSafeSpot(safeAddress);
        }
        public void leaveTry() {
            frame().leaveSafeSpot();
        }
        public void throwError() {
            reportRuntimeError(TThread.this.pop());
        }
        public void jumpTo(int address) {
            TThread.this.jumpTo(address);
        }
        public void getIterator() {
            getItr();
        }
        public void iteratorNext() {
            itrNext();
        }
        public void branchIterator(int address) {
            branchItr(address);
        }
        public void branchOn(boolean when, int address) {
            branchOnBoolean(when, address);
        }
        public void loadMember(int utf8Address) {
            unsafeMemberAccess(utf8Address);
        }
        public void storeMember(int utf8Address) {
            unsafeMemberWrite(utf8Address);
        }
        public void loadMemberFast(int address) {
            TThread.this.fastMemberAccess(address);
        }
        public void storeMemberFast(int address) {
            TThread.this.fastMemberWrite(address);
        }
        public void compare(boolean onTrue) {
            boolean b = TThread.this.pop().equals(TThread.this.pop());
            push(TBoolean.of(b == onTrue));
        }
        public void binaryOperation(Opcode operation) {
            operateBinary(operation);
        }
        public void unaryOperation(Opcode operation) {
            operateUnary(operation);
        }
        public void getType() {
            push((unpack(TThread.this.pop())).getType());
        }
        public void callSuper(int argc) {
            callSuperConstructor(argc);
        }
        public void loadAbstractMethod(int utf8Address) {
            TThread.this.loadAbstractMethod(utf8Address);
        }
        public void loadStatic(int utf8Address) {
            TThread.this.loadStatic(utf8Address);
        }
        public void storeStatic(int utf8Address) {
            TThread.this.storeStatic(utf8Address);
        }
        public void onBreakPoint() {
            DebugAction action = vm.debug(TThread.this);
            switch (action){
                case RESUME -> { /* simply run until next halt */ }
                case STEP -> interpreter = new DebugInterpreter(this);
                case QUIT -> vm.quit();
                default -> throw new IllegalStateException("unsupported DebugAction: " + action);
            }

        }
    }
    private class DebugInterpreter extends SimpleInterpreter {
        private final Interpreter prev;
        private DebugInterpreter(Interpreter prev) {
            this.prev = prev;
        }
        public void onBreakPoint() {
            haltDebug();
        }
        public void containerWrite() {
            super.containerWrite();
            haltDebug();
        }
        public void storeLocal(int address) {
            super.storeLocal(address);
            haltDebug();
        }
        public void storeGlobal(int address) {
            super.storeGlobal(address);
            haltDebug();
        }
        public void branchIterator(int address) {
            super.branchIterator(address);
            haltDebug();
        }
        public void branchOn(boolean when, int address) {
            super.branchOn(when, address);
            haltDebug();
        }
        public void storeStatic(int utf8Address) {
            super.storeStatic(utf8Address);
            haltDebug();
        }
        public void call(int argc) {
            haltDebug();
            super.call(argc);
        }
        public void callSuper(int argc) {
            haltDebug();
            super.callSuper(argc);
        }
        public void throwError() {
            haltDebug();
            super.throwError();
        }
        private void haltDebug(){
            DebugAction action = vm.debug(TThread.this);
            switch (action){
                case STEP -> { /* simply run until next halt */ }
                case RESUME -> interpreter = prev;
                case QUIT -> vm.quit();
                default -> throw new IllegalStateException("unsupported DebugAction: " + action);
            }
        }
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
        public List<FrameInfo> getFrameTrees() {
            return frameTrees;
        }
    }

}
