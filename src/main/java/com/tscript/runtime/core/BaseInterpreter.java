package com.tscript.runtime.core;

import com.tscript.projectfile.ProjectFile;
import com.tscript.runtime.stroage.FunctionArea;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.TypeArea;
import com.tscript.runtime.stroage.loading.ModuleLoader;
import com.tscript.runtime.stroage.loading.ModuleLoadingException;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;

import java.io.File;
import java.util.*;

public class BaseInterpreter implements Interpreter {

    private final TThread thread;

    public BaseInterpreter(TThread thread) {
        this.thread = thread;
    }

    @Override
    public TThread getCurrentThread() {
        return thread.getCurrentThread();
    }

    @Override
    public void reportRuntimeError(TObject message) {
        thread.reportRuntimeError(message);
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {
        return thread.call(called, arguments);
    }

    @Override
    public void pushNull() {
        thread.push(Null.INSTANCE);
    }

    @Override
    public void pushInt(byte value) {
        thread.push(new TInteger(value));
    }

    @Override
    public void pushBool(byte value) {
        thread.push(TBoolean.of(value == 1));
    }

    @Override
    public void loadConst(byte b1, byte b2) {
        Module module = thread.getFrame().getModule();
        Pool pool = module.getPool();
        thread.push(pool.loadConstant(b1, b2));
    }

    @Override
    public void loadNative(byte b1, byte b2) {
        String name = getUtf8Constant(b1, b2);

        ProjectFile projectFile = thread.getVM().projectFile;
        if (projectFile == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.noSuchNativeFunctionFound(name));
            return;
        }

        NativeFunction func = projectFile.getNative(name);

        if (func == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.noSuchNativeFunctionFound(name));
            return;
        }

        thread.push(func);
    }

    @Override
    public void loadVirtual(byte b1, byte b2) {
        Module module = thread.getFrame().getModule();
        FunctionArea functionArea = module.getFunctionArea();
        int index = Conversion.from2Bytes(b1, b2);
        VirtualFunction func = functionArea.loadFunction(index, module);
        thread.push(func);
    }

    @Override
    public void loadBuiltin(byte b, byte b1) {
        int index = Conversion.from2Bytes(b, b1);
        TObject object = Builtins.load(index);
        thread.push(object);
    }

    @Override
    public void loadType(byte b1, byte b2) {
        Module module = thread.getFrame().getModule();
        TypeArea typeArea = module.getTypeArea();
        int index = Conversion.from2Bytes(b1, b2);
        Type type = typeArea.loadType(thread, index);
        if (thread.frameStack.isEmpty())
            // exception in static block occurred
            return;
        thread.push(type);
    }

    @Override
    public void buildType(byte b1, byte b2) {
        Module module = thread.getFrame().getModule();
        TypeArea typeArea = module.getTypeArea();
        int index = Conversion.from2Bytes(b1, b2);
        VirtualType type = typeArea.loadType(thread, index);

        if (thread.frameStack.isEmpty())
            // exception in static block occurred
            return;

        TObject superType = thread.pop();
        if (type.superType == null){
            if (!(superType instanceof Type st)){
                thread.reportRuntimeError("can not bind super type because " + superType + " is not a type");
                return;
            }
            type.superType = st;
        }
        thread.push(type);
    }

    @Override
    public void pushThis() {
        thread.push(thread.getFrame().getOwner());
    }

    @Override
    public void pop() {
        thread.pop();
    }

    @Override
    public void newLine(int line) {
        thread.getFrame().setLine(line);
    }

    @Override
    public void loadGlobal(byte address) {
        Module module = thread.getFrame().getModule();
        TObject value = module.loadMember(address).get();
        thread.push(value);
    }

    @Override
    public void storeGlobal(byte address) {
        TObject value = thread.pop();
        Module module = thread.getFrame().getModule();
        module.loadMember(address).set(value, thread);
    }

    @Override
    public void loadLocal(byte address) {
        thread.push(thread.getFrame().load(address));
    }

    @Override
    public void storeLocal(byte address) {
        thread.getFrame().store(address, thread.pop());
    }

    @Override
    public void loadExternal(byte b1, byte b2) {
        TObject lookedUp = thread.pop();
        Member member = loadMemberFromExternal(lookedUp, b1, b2);
        if (member == null) return;
        thread.push(member.get());
    }

    @Override
    public void storeExternal(byte b1, byte b2) {
        TObject lookedUp = thread.pop();
        Member member = loadMemberFromExternal(lookedUp, b1, b2);
        if (member == null) return;

        if (!member.isMutable()){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidMutability(member.getName()));
            return;
        }

        member.set(thread.pop(), thread);
    }

    @Override
    public void loadInternal(byte b1, byte b2) {
        TObject owner = thread.getFrame().getOwner();
        Member member = owner.loadMember(Conversion.from2Bytes(b1, b2));
        thread.push(member.get());
    }

    @Override
    public void storeInternal(byte b1, byte b2) {
        TObject owner = thread.getFrame().getOwner();
        TObject value = thread.pop();
        Member member = owner.loadMember(Conversion.from2Bytes(b1, b2));
        member.set(value, thread);
    }

    @Override
    public void loadSuper(byte b1, byte b2) {
        TObject owner = thread.getFrame().getOwner();
        Member member = loadMemberFromSuper(owner, b1, b2);
        if (member == null) return;
        thread.push(member.get());
    }

    @Override
    public void storeSuper(byte b1, byte b2) {
        TObject owner = thread.getFrame().getOwner();
        Member member = loadMemberFromSuper(owner, b1, b2);
        if (member == null) return;

        if (!member.isMutable()){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidMutability(member.getName()));
            return;
        }

        member.set(thread.pop(), thread);
    }

    @Override
    public void loadStatic(byte b1, byte b2) {
        Member member = loadStaticMember(b1, b2);
        if (member == null) return;
        thread.push(member.get());
    }

    @Override
    public void storeStatic(byte b1, byte b2) {
        Member member = loadStaticMember(b1, b2);
        if (member == null) return;
        member.set(thread.pop(), thread);
    }

    private Member loadStaticMember(byte b1, byte b2) {
        String name = getUtf8Constant(b1, b2);
        TObject owner = thread.getFrame().getOwner();

        Type type = owner.getType();
        while (type != null) {
            Member member = type.loadMember(name);
            if (member != null) return member;
            type = type.getSuperType();
        }

        thread.reportRuntimeError(InternalRuntimeErrorMessages.canNotFindStaticMember(name));
        return null;
    }

    @Override
    public void containerRead() {
        TObject candidate = thread.pop();
        if (!(candidate instanceof ContainerAccessibleObject accessible)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notAccessible(candidate));
            return;
        }
        TObject key = thread.pop();
        TObject accessed = accessible.readFromContainer(thread, key);
        if (accessed == null)
            return;
        thread.push(accessed);
    }

    @Override
    public void containerWrite() {
        TObject candidate = thread.pop();

        if (!(candidate instanceof ContainerWriteableObject writeable)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notWriteable(candidate));
            return;
        }
        TObject key = thread.pop();
        TObject value = thread.pop();
        writeable.writeToContainer(thread, key, value);
    }

    @Override
    public void loadAbstract(byte b1, byte b2) {
        String methodName = getUtf8Constant(b1, b2);
        TObject owner = thread.getFrame().getOwner();

        if (!(owner instanceof VirtualObject vo)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.noSuchAbstractImplementationFound(methodName));
            return;
        }

        Member member = null;
        while (vo != null){
            member = vo.loadMember(methodName);
            if (member != null) break;
            vo = vo.subObject;
        }

        if (member == null || member.get() == Null.INSTANCE){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.noSuchAbstractImplementationFound(methodName));
            return;
        }

        if (!(member.get() instanceof Callable)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notCallable(member.get()));
            return;
        }

        thread.push(member.get());
    }

    @Override
    public void binaryOperation(Opcode opcode) {
        TObject right = thread.pop();
        TObject left = thread.pop();
        TObject result = ALU.performBinaryOperation(left, right, opcode, thread);
        if (result == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidBinaryOperation(left, right, opcode));
            return;
        }
        thread.push(result);
    }

    @Override
    public void not() {
        performUnaryOperation(Opcode.NOT);
    }

    @Override
    public void negate() {
        performUnaryOperation(Opcode.NEG);
    }

    @Override
    public void posivate() {
        performUnaryOperation(Opcode.POS);
    }

    private void performUnaryOperation(Opcode opcode) {
        TObject value = thread.pop();
        TObject result = ALU.performUnaryOperation(value, opcode);
        if (result == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidUnaryOperation(value, opcode));
            return;
        }
        thread.push(result);
    }

    @Override
    public void equals(boolean shouldBeEqual) {
        TObject first = thread.pop();
        TObject second = thread.pop();
        boolean areEqual = TNIUtils.areEqual(first, second);
        TBoolean b = TBoolean.of(areEqual == shouldBeEqual);
        thread.push(b);
    }

    @Override
    public void getType() {
        TObject object = thread.pop();
        Type type = object.getType();
        thread.push(type);
    }

    @Override
    public void makeArray(byte size) {
        List<TObject> content = new ArrayList<>();
        for (; size > 0; size--) {
            content.add(thread.pop());
        }
        thread.push(new TArray(content));
    }

    @Override
    public void makeDict(byte size) {
        Map<TObject, TObject> content = new LinkedHashMap<>();
        for (; size > 0; size--) {
            TObject key = thread.pop();
            TObject value = thread.pop();
            content.put(key, value);
        }
        thread.push(new TDictionary(content));
    }

    @Override
    public void makeRange() {
        TObject to = thread.pop();
        TObject from = thread.pop();

        if (!(from instanceof TInteger f)) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.canNotBuildRangeFrom(from));
            return;
        }

        if (!(to instanceof TInteger t)) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.canNotBuildRangeFrom(to));
            return;
        }

        Range range = new Range(f, t);
        thread.push(range);
    }

    @Override
    public void jumpTo(byte b1, byte b2) {
        thread.getFrame().jumpTo(Conversion.from2Bytes(b1, b2));
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {
        TObject object = thread.pop();
        if (TNIUtils.isTrue(object) == ifTrue) {
            jumpTo(b1, b2);
        }
    }

    @Override
    public void getIterator() {
        TObject candidate = thread.pop();
        if (!(candidate instanceof IterableObject iterable)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notIterable(candidate));
            return;
        }
        thread.push(iterable.iterator());
    }

    @Override
    public void branchIterator(byte b1, byte b2) {
        IteratorObject itr = (IteratorObject) thread.pop();
        if (!itr.hasNext()){
            jumpTo(b1, b2);
            return;
        }
        thread.push(itr);
    }

    @Override
    public void iteratorNext() {
        IteratorObject itr = (IteratorObject) thread.pop();
        thread.push(itr);
        thread.push(itr.next());
    }

    @Override
    public void enterTry(byte b1, byte b2) {
        thread.getFrame().enterUnsafeSpot(Conversion.from2Bytes(b1, b2));
    }

    @Override
    public void leaveTry() {
        thread.getFrame().leaveUnsafeSpot();
    }

    @Override
    public void throwError() {
        TObject errorMessage = thread.pop();
        thread.reportRuntimeError(errorMessage);
    }

    @Override
    public void callInplace(byte argCount) {

        Callable called = checkCall(thread.pop());
        if (called == null) return;

        List<TObject> args = new ArrayList<>(3); // avg. param amount
        for (int i = 0; i < argCount; i++) args.add(thread.pop());
        TObject res = called.call(thread, args);
        if (res != null && thread.frameStack.size() > thread.frameStackExitThreshold)
            thread.push(res);
    }

    @Override
    public void callMapped(byte argCount) {
        Callable called = checkCall(thread.pop());
        if (called == null) return;

        // choose 3 as the most avg. max. number of arguments to
        // reduce the amount of array growth in ArrayList.
        List<String> names = new ArrayList<>(3);
        List<TObject> values = new ArrayList<>(3);

        for (int i = 0; i < argCount; i++) {
            Argument arg = (Argument) thread.pop();
            names.add(arg.getValue().getFirst());
            values.add(arg.getValue().getSecond());
        }

        TObject res = called.call(thread, names, values);
        if (res != null && thread.frameStack.size() > thread.frameStackExitThreshold)
            thread.push(res);
    }

    @Override
    public void toInplaceArgument() {
        TObject value = thread.pop();
        Argument arg = new Argument(null, value);
        thread.push(arg);
    }

    @Override
    public void toMappedArgument(byte b1, byte b2) {
        TObject value = thread.pop();
        String name = getUtf8Constant(b1, b2);
        Argument arg = new Argument(name, value);
        thread.push(arg);
    }

    private Callable checkCall(TObject candidate){

        if (isStackOverflowError())
            return null;

        if (!(candidate instanceof Callable c)) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notCallable(candidate));
            return null;
        }

        return c;
    }

    private boolean isStackOverflowError(){
        if (thread.frameStack.size() == 30_000){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.stackOverflowError());
            return true;
        }
        return false;
    }

    @Override
    public void returnFunction() {
        TObject returnValue = thread.pop();
        thread.frameStack.remove();
        thread.returnValue = returnValue;
        if (thread.frameStack.size() <= thread.frameStackExitThreshold) return;
        thread.push(returnValue);
    }

    @Override
    public void callSuper(byte argCount) {
        TObject owner = thread.getFrame().getOwner();
        if (!(owner instanceof VirtualObject vobj)){
            thread.reportRuntimeError("can not bind to builtin or native type");
            return;
        }
        if (vobj.superObject != null){
            thread.reportRuntimeError("super has already been called");
            return;
        }
        Type type = owner.getType();
        Type superType = type.getSuperType();
        assert superType != null; // should have been checked by the compiler earlier
        List<TObject> args = new ArrayList<>(); // avg. param amount
        for (int i = 0; i < argCount; i++) args.add(thread.pop());
        boolean isAbstract = superType.isAbstract();
        if (superType instanceof VirtualType vt) vt.isAbstract = false;
        TObject superObj = thread.call(superType, args);
        if (superType instanceof VirtualType vt) vt.isAbstract = isAbstract;
        if (superObj == null) return;
        vobj.superObject = superObj;
        if (superObj instanceof VirtualObject vo) vo.subObject = vobj;
    }

    @Override
    public void importModule(byte b1, byte b2) {
        String[] modulePath = getUtf8Constant(b1, b2).split("[.]");
        File[] rootPaths = thread.getVM().getRootPaths();
        ModuleLoader loader = thread.getVM().getSharedModuleLoader();
                try {
            Module loadedModule = loader.loadModule(rootPaths, modulePath);
            if (!loadedModule.isEvaluated()){
                loadedModule.setEvaluated();
                TObject returnValue = thread.call(loadedModule.getEntryPoint(), List.of());
                if (returnValue == null || thread.frameStack.isEmpty())
                    // error occurred
                    return;
            }
            thread.push(loadedModule);
        }
        catch (ModuleLoadingException ex){
            thread.reportRuntimeError(ex.getMessage());
        }
    }

    @Override
    public void useMembers() {
        TObject used = thread.pop();

        for (Member member : used.getMembers()){
            storeMember(member.getName(), member.get());
        }
    }

    @Override
    public void use(byte b1, byte b2) {
        String name = getUtf8Constant(b1, b2);
        TObject value = thread.pop();
        storeMember(name, value);
    }

    private void storeMember(String name, TObject value){
        Frame frame = thread.getFrame();
        boolean success = frame.storeName(name, value);
        if (!success){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.nameAlreadyExists(name));
        }
    }

    @Override
    public void loadName(byte b1, byte b2) {
        String name = getUtf8Constant(b1, b2);

        TObject value = thread.getFrame().loadName(name);
        if (value != null){
            thread.push(value);
            return;
        }

        if (thread.getFrame().getOwner() != null){
            TObject curr = thread.getFrame().getOwner();
            Member found = curr.loadMember(name);
            if (found != null){
                thread.push(found.get());
                return;
            }
            while (curr != null){
                TObject superObj = curr.getSuper();
                Member member = TNIUtils.searchMember(superObj, name);
                if (member != null && member.getVisibility().ordinal() <= Visibility.PROTECTED.ordinal()){
                    thread.push(member.get());
                    return;
                }
                curr = curr.getSuper();
            }
        }

        Member member = thread.getFrame().getModule().loadMember(name);
        if (member != null){
            thread.push(member.get());
            return;
        }

        int builtinIndex = Builtins.indexOf(name);
        if (builtinIndex != -1){
            value = Builtins.load(builtinIndex);
            thread.push(value);
            return;
        }

        thread.reportRuntimeError(InternalRuntimeErrorMessages.canNotFind(name));
    }

    @Override
    public void setOwner() {
        TObject candidate = thread.pop();
        if (!(candidate instanceof Function func)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.typeExpected("Function", candidate.getType().getName()));
            return;
        }

        TObject owner = thread.pop();
        func.setOwner(owner);
        thread.push(func);
    }

    @Override
    public void dup() {
        TObject top = thread.pop();
        thread.push(top);
        thread.push(top);
    }

    @Override
    public void extend(byte b1, byte b2) {
        TObject what = thread.pop();
        TObject top = thread.pop();

        String memberName = getUtf8Constant(b1, b2);
        Member member = Member.of(Visibility.PRIVATE, true, memberName, what);

        try {
            top.addMember(member);
        }
        catch (UnsupportedOperationException ex){
            thread.reportRuntimeError("can not extend " + top.getType().getDisplayName());
        }
    }

    private Member loadMemberFromExternal(TObject lookedUp, byte b1, byte b2) {
        String memberName = getUtf8Constant(b1, b2);
        Member member = TNIUtils.searchMember(lookedUp, memberName);
        return requireMember(lookedUp, memberName, member, Visibility.PUBLIC);
    }


    private Member loadMemberFromSuper(TObject subObject, byte b1, byte b2){
        TObject superObj = subObject.getSuper();
        String memberName = getUtf8Constant(b1, b2);
        Member member = TNIUtils.searchMember(superObj, memberName);
        return requireMember(superObj, memberName, member, Visibility.PROTECTED);
    }


    private Member requireMember(TObject accessed, String name, Member member, Visibility minimumVisibility){

        if (member == null) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.noSuchMember(accessed, name));
            return null;
        }

        if (member.getVisibility().ordinal() > minimumVisibility.ordinal()) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidAccessVisibility(name, member.getVisibility()));
            return null;
        }

        return member;
    }


    private String getUtf8Constant(byte b1, byte b2){
        Module module = thread.getFrame().getModule();
        Pool pool = module.getPool();
        return pool.loadName(b1, b2);
    }

}
