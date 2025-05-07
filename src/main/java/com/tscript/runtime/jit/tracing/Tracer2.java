package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRBuilder;
import com.tscript.runtime.jit.ir.nodes.*;
import com.tscript.runtime.jit.ir.nodes.Range;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.typing.Type;
import com.tscript.runtime.utils.Conversion;

import java.util.*;

public class Tracer2 extends InterpreterDecorator {

    private final Profiler profiler;
    private final TracerConfiguration configuration;
    private final IRBuilder irBuilder;

    public Tracer2(Interpreter interpreter,
                   Profiler profiler,
                   IRBuilder irBuilder,
                   TracerConfiguration configuration) {
        super(interpreter);
        this.profiler = profiler;
        this.irBuilder = irBuilder;
        this.configuration = configuration;
    }

    @Override
    public void pushNull() {
        irBuilder.nullValue();
        super.pushNull();
    }

    @Override
    public void pushInt(byte value) {
        irBuilder.integer(value);
        super.pushInt(value);
    }

    @Override
    public void pushBool(byte value) {
        irBuilder.bool(value == 1);
        super.pushBool(value);
    }

    @Override
    public void loadConst(byte b1, byte b2) {
        super.loadConst(b1, b2);
        TObject top = top();
        if (top == null) return;
        IRValue value = fromObject(top);
        irBuilder.push(value);
    }

    @Override
    public void pushThis() {
        irBuilder.makeThis();
        super.pushThis();
    }

    @Override
    public void makeRange() {
        irBuilder.range();
        super.makeRange();
    }

    @Override
    public void loadType(byte b1, byte b2) {
        super.loadType(b1, b2);
        irBuilder.loadType(Conversion.from2Bytes(b1, b2));
    }

    @Override
    public void buildType(byte b1, byte b2) {
        super.buildType(b1, b2);
        irBuilder.loadType(Conversion.from2Bytes(b1, b2));
    }

    @Override
    public void makeArray(byte size) {
        irBuilder.array(size);
        super.makeArray(size);
    }

    @Override
    public void makeDict(byte size) {
        irBuilder.dict(size);
        super.makeDict(size);
    }

    @Override
    public void loadVirtual(byte b1, byte b2) {
        super.loadVirtual(b1, b2);
        int address = Conversion.from2Bytes(b1, b2);
        irBuilder.virtual(address);
    }

    @Override
    public void loadNative(byte b1, byte b2) {
        super.loadNative(b1, b2);
        NativeFunction function = (NativeFunction) top();
        if (function == null) return;
        irBuilder.makeNative(function.getName());
    }

    @Override
    public void loadBuiltin(byte b1, byte b2) {
        int index = Conversion.from2Bytes(b1, b2);
        irBuilder.builtin(index);
        super.loadBuiltin(b1, b2);
    }

    @Override
    public void callInplace(byte argCount) {
        TObject top = top();

        if (top instanceof Callable called) {
            IRValue calledIRNode = irBuilder.pop();

            List<TObject> missing = getMissingInplaceArgs(called, argCount);
            if (missing == null) return;
            for (TObject o : missing) {
                irBuilder.push(fromObject(o));
            }

            irBuilder.push(calledIRNode);

            irBuilder.callInp(argCount + missing.size(), top instanceof VirtualFunction);
        }

        super.callInplace(argCount);
    }

    private List<TObject> getMissingInplaceArgs(Callable callable, int argCount) {
        Parameters parameters = callable.getParameters(getCurrentThread());
        int lastNonDefined = parameters.getLastNonDefinedParameterIndex();
        if (argCount <= lastNonDefined) return null;
        return parameters.getTail(argCount);
    }

    @Override
    public void callMapped(byte argCount) {
        TObject top = top();

        if (top instanceof Callable called) {
            IRValue calledIRNode = irBuilder.pop();

            IRValue[] flattenArgs = flatMappedCallArgs(called, argCount);
            if (flattenArgs != null) {
                for (IRValue flattenArg : flattenArgs) {
                    irBuilder.push(flattenArg);
                }
                irBuilder.push(calledIRNode);
                irBuilder.callInp(flattenArgs.length, top instanceof VirtualFunction);
            }
            // else invalid parameter passing
        }

        super.callMapped(argCount);
    }

    private IRValue[] flatMappedCallArgs(Callable called, int argCount) {
        List<String> names = new ArrayList<>(argCount);
        List<IRValue> values = new ArrayList<>(argCount);

        for (int i = 0; i < argCount; i++) {
            IRValue exp = irBuilder.pop();
            if (exp instanceof ToInpArg t) {
                names.add(null);
                values.add(t.value);
            } else {
                ToMapArg m = (ToMapArg) exp;
                names.add(m.name);
                values.add(m.value);
            }
        }

        Parameters parameters = called.getParameters(getCurrentThread());

        if (names.size() > parameters.count()) return null;

        LinkedHashMap<String, IRValue> finals = new LinkedHashMap<>();

        int index = 0;

        Iterator<IRValue> valueItr = values.iterator();
        for (String name : names) {
            if (name != null) {
                if (!parameters.hasName(name)) return null;
                boolean success = finals.put(name, valueItr.next()) == null;
                if (!success) return null;
            } else {
                String nextParam = parameters.getNames().get(index);
                boolean success = finals.put(nextParam, valueItr.next()) == null;
                if (!success) return null;
            }
            index++;
        }

        for (Map.Entry<String, TObject> defaultParam : parameters.toMap().entrySet()) {
            if (!finals.containsKey(defaultParam.getKey()) && defaultParam.getValue() != null)
                finals.put(defaultParam.getKey(), fromObject(defaultParam.getValue()));
        }

        if (finals.size() < parameters.count()) return null;

        return finals.values().toArray(new IRValue[0]);
    }

    @Override
    public void toInplaceArgument() {
        irBuilder.toInpArg();
        super.toInplaceArgument();
    }

    @Override
    public void toMappedArgument(byte b1, byte b2) {
        String name = getUtf8Constant(b1, b2);
        irBuilder.toMapArg(name);
        super.toMappedArgument(b1, b2);
    }

    @Override
    public void pop() {
        irBuilder.makeExprStmt();
        super.pop();
    }

    @Override
    public void dup() {
        irBuilder.dup();
        super.dup();
    }

    @Override
    public void jumpTo(byte b1, byte b2) {
        super.jumpTo(b1, b2);

        final TThread thread = getCurrentThread();

        if (configuration.isLoopFocused() && configuration.isTraceComplete(thread)) {
            complete();
            return;
        }

        int address = Conversion.from2Bytes(b1, b2);
        irBuilder.createGoto(address);
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {
        int address = Conversion.from2Bytes(b1, b2);
        irBuilder.createBranch(address, ifTrue);
        super.branch(b1, b2, ifTrue);
    }

    @Override
    public void newLine(int line) {
        irBuilder.newLine(line);
        super.newLine(line);
    }

    @Override
    public void loadLocal(byte address) {
        irBuilder.loadLocal(address);
        super.loadLocal(address);
    }

    @Override
    public void storeLocal(byte address) {
        super.storeLocal(address);
        if (configuration.isParameterStorage()) {
            return;
        }
        irBuilder.storeLocal(address);
    }

    @Override
    public void loadGlobal(byte address) {
        irBuilder.loadGlobal(address);
        super.loadGlobal(address);
    }

    @Override
    public void storeGlobal(byte address) {
        irBuilder.storeGlobal(address);
        super.storeGlobal(address);
    }

    @Override
    public void loadInternal(byte b1, byte b2) {
        irBuilder.loadInternal(Conversion.from2Bytes(b1, b2));
        super.loadInternal(b1, b2);
    }

    @Override
    public void storeInternal(byte b1, byte b2) {
        irBuilder.storeInternal(Conversion.from2Bytes(b1, b2));
        super.storeInternal(b1, b2);
    }

    @Override
    public void loadExternal(byte b1, byte b2) {
        irBuilder.loadExternal(getUtf8Constant(b1, b2));
        super.loadExternal(b1, b2);
    }

    @Override
    public void storeExternal(byte b1, byte b2) {
        irBuilder.storeExternal(getUtf8Constant(b1, b2));
        super.storeExternal(b1, b2);
    }

    @Override
    public void loadStatic(byte b1, byte b2) {
        irBuilder.loadStatic(getUtf8Constant(b1, b2));
        super.loadStatic(b1, b2);
    }

    @Override
    public void storeStatic(byte b1, byte b2) {
        irBuilder.storeStatic(getUtf8Constant(b1, b2));
        super.storeStatic(b1, b2);
    }

    @Override
    public void loadSuper(byte b1, byte b2) {
        irBuilder.loadSuper(getUtf8Constant(b1, b2));
        super.loadSuper(b1, b2);
    }

    @Override
    public void storeSuper(byte b1, byte b2) {
        irBuilder.storeSuper(getUtf8Constant(b1, b2));
        super.storeSuper(b1, b2);
    }

    @Override
    public void loadName(byte b1, byte b2) {
        irBuilder.loadName(getUtf8Constant(b1, b2));
        super.loadName(b1, b2);
    }

    @Override
    public void loadAbstract(byte b1, byte b2) {
        irBuilder.loadAbstract(getUtf8Constant(b1, b2));
        super.loadAbstract(b1, b2);
    }

    @Override
    public void containerRead() {
        irBuilder.containerRead();
        super.containerRead();
    }

    @Override
    public void containerWrite() {
        irBuilder.containerWrite();
        super.containerWrite();
    }

    @Override
    public void getIterator() {
        irBuilder.getItr();
        super.getIterator();
    }

    @Override
    public void iteratorNext() {
        irBuilder.itrNext();
        super.iteratorNext();
    }

    @Override
    public void branchIterator(byte b1, byte b2) {
        int prevIp = ip();
        super.branchIterator(b1, b2);
        int curIp = ip();

        boolean hasNext = (prevIp == curIp);

        irBuilder.itrHasNext(hasNext);
        irBuilder.createGuard(ip()-1, sp()+1);
    }

    @Override
    public void getType() {
        irBuilder.typeof();
        super.getType();
    }

    @Override
    public void equals(boolean shouldBeEqual) {
        irBuilder.equals(shouldBeEqual);
        super.equals(shouldBeEqual);
    }

    @Override
    public void negate() {
        TObject topObj = top();

        if (topObj != null) {
            typeGuard(irBuilder.top(), topObj);
        }
        irBuilder.negate();
        super.negate();
    }

    @Override
    public void not() {
        TObject topObj = top();

        if (topObj != null) {
            typeGuard(irBuilder.top(), topObj);
        }
        irBuilder.not();
        super.not();
    }

    @Override
    public void posivate() {
        TObject topObj = top();
        if (topObj != null) {
            typeGuard(irBuilder.pop(), topObj);
        }
        super.posivate();
    }

    @Override
    public void binaryOperation(Opcode opcode) {
        IRValue rhs = irBuilder.pop();
        IRValue lhs = irBuilder.pop();
        irBuilder.push(lhs);
        irBuilder.push(rhs);

        TObject rhsObj = top(0);
        TObject lhsObj = top(1);

        if (rhsObj != null) {
            typeGuard(rhs, rhsObj);
        }
        if (lhsObj != null) {
            typeGuard(lhs, lhsObj);
        }

        irBuilder.binaryOp(opcode);
        super.binaryOperation(opcode);
    }

    @Override
    public void importModule(byte b1, byte b2) {
        super.importModule(b1, b2);

        String[] modulePath = getUtf8Constant(b1, b2).split("[.]");
        int index = getCurrentThread().getVM().getSharedModuleLoader().indexOf(modulePath);
        if (index != -1) {
            irBuilder.importModule(index);
        }
    }

    @Override
    public void callSuper(byte argCount) {
        super.callSuper(argCount);
    }

    @Override
    public void setOwner() {
        irBuilder.setOwner();
        super.setOwner();
    }

    @Override
    public void use(byte b1, byte b2) {
        irBuilder.use(getUtf8Constant(b1, b2));
        super.use(b1, b2);
    }

    @Override
    public void useMembers() {
        irBuilder.useMembers();
        super.useMembers();
    }

    @Override
    public void extend(byte b1, byte b2) {
        irBuilder.extend(getUtf8Constant(b1, b2));
        super.extend(b1, b2);
    }

    @Override
    public void returnFunction() {
        super.returnFunction();

        final TThread thread = getCurrentThread();

        if (configuration.isFunctionFocused() && configuration.isTraceComplete(thread)) {
            irBuilder.makeReturn();
            complete();
        }
        else {
            irBuilder.storeTemp(0);
            irBuilder.makePopFrameReturn();
            irBuilder.loadTemp(0);
        }
    }

    @Override
    public void enterTry(byte b1, byte b2) {
        // no tracing required
        super.enterTry(b1, b2);
    }

    @Override
    public void leaveTry() {
        // no tracing required
        super.leaveTry();
    }

    @Override
    public void throwError() {
        // no tracing required
        super.throwError();
    }

    private TObject top(){
        return top(0);
    }

    private TObject top(int offset){
        TThread thread = getCurrentThread();
        if (thread.frameStack.isEmpty()) return null;
        Frame frame = thread.frameStack.element();
        return frame.stack[frame.sp-1-offset];
    }

    private String getUtf8Constant(byte b1, byte b2){
        Module module = getCurrentThread().frameStack.element().getModule();
        Pool pool = module.getPool();
        return pool.loadName(b1, b2);
    }

    private IRValue fromObject(TObject obj) {
        if (obj instanceof TString s) return new Str(s.getValue());
        else if (obj instanceof TReal r) return new Real(r.getValue());
        else if (obj instanceof TBoolean b) return new Bool(b.getValue());
        else if (obj instanceof TInteger i) return new Int(i.getValue());
        else if (obj == com.tscript.runtime.typing.Null.INSTANCE) return new NullValue();
        else if (obj instanceof com.tscript.runtime.typing.Range r) return new Range(new Int(r.getFrom()), new Int(r.getTo()));
        else if (obj instanceof TArray a) {
            List<IRValue> exps = new ArrayList<>();
            for (TObject entry : a.getValue()){
                exps.add(new WrapTObj(fromObject(entry)));
            }
            return new Array(exps);
        }
        else if (obj instanceof TDictionary d){
            Dict dict = new Dict();
            for (Map.Entry<TObject, TObject> entry : d.getValue().entrySet()){
                dict.keys.add(new WrapTObj(fromObject(entry.getKey())));
                dict.values.add(new WrapTObj(fromObject(entry.getValue())));
            }
            return dict;
        }
        else throw new AssertionError();
    }

    private int sp(){
        return getCurrentThread().frameStack.element().sp;
    }

    private int ip(){
        return getCurrentThread().frameStack.element().ip;
    }

    private void typeGuard(IRValue given, TObject expectedTypeInstance) {
        // if (true) return;

        irBuilder.push(given);
        irBuilder.typeof();

        Type type = expectedTypeInstance.getType();
        irBuilder.loadType(type);

        irBuilder.equals(true);
        irBuilder.createGuard(ip(), sp());
    }

    private void complete() {
        TThread thread = getCurrentThread();
        JITCompiler jit = thread.getVM().getJitCompiler();

        IRBlock trace = irBuilder.getRoot();
        JITTask task = configuration.completeTrace(thread, trace);

        jit.handle(task);
        thread.setInterpreter(profiler);
    }

}
