package runtime.core;

import runtime.heap.Heap;
import runtime.jit.JIT;
import runtime.jit.JITSensitive;
import runtime.tni.NativePrint;
import runtime.type.*;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.*;

public class TThread extends Thread {

    private final TscriptVM vm;
    private final Callable baseFunction;
    private final ArrayDeque<Frame> frameStack = new ArrayDeque<>();
    private final int threadID;

    private int frameExitThreshold = 0;
    private Data returnValue;


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
        while (frameStack.size() > frameExitThreshold)
            processNext();
    }

    protected void processNext(){
        byte[] instruction = frameStack.element().fetch();
        decodeAndExecute(instruction);
    }

    private void decodeAndExecute(byte[] instruction){
        Opcode opcode = Opcode.of(instruction[0]);
        switch (opcode){
            case PUSH_NULL -> push(TNull.NULL);
            case PUSH_INT -> push(new TInteger((int) instruction[1]));
            case PUSH_BOOL -> push(TBoolean.of(instruction[1]));
            case STORE_GLOBAL -> storeGlobal(instruction[1]);
            case LOAD_GLOBAL -> push(vm.loadGlobal(instruction[1]));
            case STORE_LOCAL -> storeLocal(instruction[1]);
            case LOAD_LOCAL -> push(frame().load(instruction[1]));
            case LOAD_CONST -> loadConst(instruction[1]);
            case CONTAINER_READ -> containerRead();
            case CONTAINER_WRITE -> containerWrite();
            case RETURN -> returnVirtual();
            case WRAP_ARGUMENT -> wrapArgument(instruction[1]);
            case CALL -> call(instruction[1]);
            case POP -> pop();
            case MAKE_RANGE -> makeRange();
            case MAKE_ARRAY -> makeArray(instruction[1]);
            case MAKE_DICT -> makeDict(instruction[1]);
            case ENTER_TRY -> frame().enterSafeSpot(instruction[1]);
            case LEAVE_TRY -> frame().leaveSafeSpot();
            case THROW -> reportRuntimeError(pop());
            case GOTO -> jumpTo(instruction[1], instruction[2]);
            case GET_ITR -> getItr();
            case ITR_NEXT -> itrNext();
            case BRANCH_ITR -> branchItr(instruction[1], instruction[2]);
            case BRANCH_IF_FALSE -> branchOnBoolean(false, instruction[1], instruction[2]);
            case BRANCH_IF_TRUE -> branchOnBoolean(true, instruction[1], instruction[2]);
            case LOAD_MEMBER -> unsafeMemberAccess(instruction[1]);
            case STORE_MEMBER -> unsafeMemberWrite(instruction[1]);
            case LOAD_MEMBER_FAST -> fastMemberAccess(instruction[1]);
            case STORE_MEMBER_FAST -> fastMemberWrite(instruction[1]);
            case EQUALS -> push(TBoolean.of(pop().equals(pop())));
            case NOT_EQUALS -> push(TBoolean.of(!pop().equals(pop())));
            case ADD, SUB, MUL, DIV, IDIV, MOD, POW,
                    AND, OR, XOR, LT, GT, LEQ, GEQ,
                    SLA, SRA, SRL -> operateBinary(opcode);
            case NOT, NEG, POS -> operateUnary(opcode);
            case PUSH_THIS -> push(frame().getOwner());
            case GET_TYPE -> push((unpack(pop())).getType());
            case CALL_SUPER -> callSuperConstructor(instruction[1]);
            case LOAD_ABSTRACT_IMPL -> loadAbstractMethod(instruction[1]);
            case LOAD_STATIC -> loadStatic(instruction[1]);
            case STORE_STATIC -> storeStatic(instruction[1]);
            case BREAK_POINT -> vm.debug(this);
            case NEW_LINE -> setLine(Conversion.fromBytes(
                    instruction[1],
                    instruction[2],
                    instruction[3],
                    instruction[4]));

            default ->
                    throw new IllegalStateException("invalid opcode " + opcode + " 0x" + Integer.toHexString(instruction[0]));
        }
    }

    private Frame frame(){
        return frameStack.element();
    }

    private void storeGlobal(byte addr){
        Data dataToWrite = pop();
        Data displaced = vm.storeGlobal(addr, dataToWrite);
        reassignValue(displaced, dataToWrite);
    }

    private void storeLocal(byte addr){
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

    private void loadStatic(byte poolAddr){
        String memberName = (String) loadFromPool(poolAddr);
        Member member = searchStaticMember(memberName);
        if (member == null) return;
        push(member.data);
    }

    private void storeStatic(byte poolAddr){
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
        if (member.data == null){
            // member was not initialized yet
            member.kind = unpack(assigned) instanceof Callable
                    ? Member.Kind.IMMUTABLE
                    : Member.Kind.MUTABLE;
        }
        member.data = assigned;
    }

    private void loadAbstractMethod(byte poolAddr){
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

    private void jumpTo(byte b1, byte b2){
        int addr = ((b1 & 0xff) << 8) | (b2 & 0xff);
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

    private void branchItr(byte b1, byte b2){
        IteratorObject itr = (IteratorObject) pop();
        if (!itr.hasNext()) {
            jumpTo(b1, b2);
            return;
        }
        push(itr);
    }

    private void branchOnBoolean(boolean when, byte b1, byte b2){
        Data data = pop();
        if (isTrue(data) == when){
            jumpTo(b1, b2);
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

    private void wrapArgument(byte poolAddr){
        String refName = (String) loadFromPool(poolAddr);
        push(new Argument(refName, pop()));
    }

    private void call(byte argc){

        if (!checkStackOverflowError())
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

    private void callSuperConstructor(byte argc){
        Frame frame = frame();
        TObject owner = unpack(frame.getOwner());
        TType type = owner.getType();
        TType superType = type.getSuper();
        Callable superConstructor = superType.getConstructor();
        superConstructor.setOwner(frame.getOwner());
        List<Argument> argList = getCallerArguments(argc);
        superConstructor.call(this, argList);
    }

    private List<Argument> getCallerArguments(byte argc){
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
        if (!checkStackOverflowError()) return null;
        return callFromNativeContext(callable, args);
    }

    @JITSensitive
    public void setLine(int line){
        frame().setLine(line);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkStackOverflowError(){
        if (frameStack.size() == 30_000){
            reportRuntimeError("stackOverflowError");
            return false;
        }
        return true;
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

    public void gc(){
        vm.gc(this);
    }

    public JIT getJIT(){
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


}
