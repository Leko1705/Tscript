package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.graph.EndNode;
import com.tscript.runtime.jit.graph.FlowNode;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.expr.Null;
import com.tscript.runtime.jit.graph.expr.Range;
import com.tscript.runtime.jit.graph.expr.Type;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;

import java.util.*;

public class Tracer extends InterpreterDecorator {

    private static final int MAX_TRACE_LIMIT = 1000;

    private final Profiler profiler;
    private final int callStackDepth;
    private final FlowNode root;
    private Node current;
    private int traceSize;

    private final Deque<Expression> stack;
    private int argsLeft;
    private final TracingConfiguration configuration;

    public Tracer(Interpreter interpreter,
                  Profiler profiler,
                  int callStackDepth,
                  TracingConfiguration configuration) {
        super(interpreter);
        this.profiler = profiler;
        this.callStackDepth = callStackDepth;
        this.configuration = configuration;

        root = new FlowNode(Expression.EMPTY);
        root.next = root;
        current = root;
        stack = new ArrayDeque<>();
        traceSize = 0;
        argsLeft = configuration.paramStorageSkips();
    }

    @Override
    public void pushNull() {
        super.pushNull();
        stack.push(new Null());
    }

    @Override
    public void pushInt(byte value) {
        super.pushInt(value);
        stack.push(new Int((int)value));
    }

    @Override
    public void pushBool(byte value) {
        super.pushBool(value);
        stack.push(new Bool(value == 1));
    }

    @Override
    public void loadConst(byte b1, byte b2) {
        super.loadConst(b1, b2);
        TObject top = top();
        if (top == null) return;
        Expression exp = fromObject(top);
        stack.push(exp);
    }

    private Expression fromObject(TObject obj) {
        if (obj instanceof TString s) return new Str(s.getValue());
        else if (obj instanceof TReal r) return new Real(r.getValue());
        else if (obj instanceof TBoolean b) return new Bool(b.getValue());
        else if (obj instanceof TInteger i) return new Int(i.getValue());
        else if (obj == com.tscript.runtime.typing.Null.INSTANCE) return new Null();
        else if (obj instanceof com.tscript.runtime.typing.Range r) return new Range(new Int(r.getFrom()), new Int(r.getTo()), false);
        else if (obj instanceof TArray a) {
            List<Expression> exps = new ArrayList<>();
            for (TObject entry : a.getValue()){
                exps.add(fromObject(entry));
            }
            return new Array(exps);
        }
        else if (obj instanceof TDictionary d){
            Dict dict = new Dict();
            for (Map.Entry<TObject, TObject> entry : d.getValue().entrySet()){
                dict.keys.add(fromObject(entry.getKey()));
                dict.values.add(fromObject(entry.getValue()));
            }
            return dict;
        }
        else throw new AssertionError();
    }

    @Override
    public void pushThis() {
        super.pushThis();
        stack.push(This.INSTANCE);
    }

    @Override
    public void makeRange() {
        super.makeRange();
        stack.push(new Range(stack.remove(), stack.remove(), false));
    }

    @Override
    public void makeArray(byte size) {
        super.makeArray(size);
        List<Expression> exps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            exps.add(stack.remove());
        }
        stack.push(new Array(exps));
    }

    @Override
    public void makeDict(byte size) {
        super.makeDict(size);
        Dict dict = new Dict();
        for (int i = 0; i < size; i++) {
            dict.keys.add(stack.remove());
            dict.values.add(stack.remove());
        }
        stack.push(dict);
    }

    @Override
    public void loadNative(byte b1, byte b2) {
        super.loadNative(b1, b2);
        NativeFunction function = (NativeFunction) top();
        if (function == null) return;
        stack.push(new Native(function.getName(), false));
    }

    @Override
    public void loadVirtual(byte b1, byte b2) {
        super.loadVirtual(b1, b2);
        stack.push(new Virtual(Conversion.from2Bytes(b1, b2)));
    }

    @Override
    public void loadBuiltin(byte b1, byte b2) {
        super.loadBuiltin(b1, b2);
        stack.push(new Builtin(Conversion.from2Bytes(b1, b2)));
    }

    @Override
    public void loadAbstract(byte b1, byte b2) {
        super.loadAbstract(b1, b2);
        stack.push(new Abstract(getUtf8Constant(b1, b2), false));
    }

    @Override
    public void setOwner() {
        super.setOwner();
        Expression func = stack.remove();
        Expression owner = stack.remove();
        stack.push(new Method(owner, func));
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {
        // Method is called from native context.
        // Ensure the optimized version is called instead.
        return profiler.call(called, arguments);
    }

    @Override
    public void callInplace(byte argCount) {

        Frame frame = getCurrentThread().frameStack.element();
        TObject top = frame.pop();

        if (top instanceof VirtualFunction vfunc) {
            boolean nativeImageExecuted = callNativeImageInplace(argCount, frame, vfunc);

            if (nativeImageExecuted) {
                List<TObject> missingDefaults = getMissingInplaceArgs(vfunc, argCount);
                if (missingDefaults == null) return;
                Expression funcExpr = stack.remove();
                List<Expression> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) args.add(stack.remove());
                for (TObject missing : missingDefaults) args.add(fromObject(missing));
                stack.push(new CallImage(funcExpr, args));
                return;
            }
        }

        frame.push(top);
        super.callInplace(argCount);

        if (top instanceof Callable callable) {

            Expression func = stack.remove();
            List<TObject> missingDefaults = getMissingInplaceArgs(callable, argCount);
            if (missingDefaults == null) return;

            if (callable.isVirtual()) {

                for (int i = 0; i < argCount; i++)
                    extendFlow(new StoreTemp(stack.remove(), i));

                int j = 0;
                for (TObject missing : missingDefaults) {
                    extendFlow(new StoreTemp(fromObject(missing), argCount + j));
                    j++;
                }

                extendFlow(new PushFrame(func, callable instanceof com.tscript.runtime.typing.Type));

                for (int i = 0; i < argCount + missingDefaults.size(); i++)
                    stack.push(new LoadTemp(i));
            }
            else {
                List<Expression> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) args.add(stack.remove());
                for (TObject missing : missingDefaults) args.add(fromObject(missing));
                stack.push(new CallInp(func, args));

            }
        }
    }

    private boolean callNativeImageInplace(int argCount, Frame frame, VirtualFunction vfunc) {
        TObject[] args = new TObject[argCount];
        com.tscript.runtime.typing.Type[] types = new com.tscript.runtime.typing.Type[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = frame.pop();
            types[i] = args[i].getType();
        }

        JITCompiler compiler = getCurrentThread().getVM().getJitCompiler();
        JITCache cache = compiler.getCache();

        NativeImage image = cache.getFunctional(vfunc, types);

        if (image == null) {
            for (int i = argCount - 1; i >= 0; i--) frame.push(args[i]);
            return false;
        }

        ImageFrame imgFrame = new ImageFrame(vfunc, args);
        try {
            TObject result = image.run(super.getCurrentThread(), imgFrame);
            if (result != null) {
                frame.push(result);
            }
        } catch (FallbackException ignored) {
            if (Profiler.DEBUG) {
                System.err.println("Fallback exception");
            }
        }

        return true;
    }

    private List<TObject> getMissingInplaceArgs(Callable callable, int argCount) {
        Parameters parameters = callable.getParameters(getCurrentThread());
        int lastNonDefined = parameters.getLastNonDefinedParameterIndex();
        if (argCount <= lastNonDefined) return null;
        return parameters.getTail(argCount);
    }

    @Override
    public void callMapped(byte argCount) {
        Frame frame = getCurrentThread().frameStack.element();
        TObject top = frame.pop();
        frame.push(top);

        super.callMapped(argCount);

        if (top instanceof Callable callable && callable.isVirtual()) {
            Expression func = stack.remove();

            if (callable.isVirtual()) {

                List<String> names = new ArrayList<>(argCount);
                List<Expression> values = new ArrayList<>(argCount);

                for (int i = 0; i < argCount; i++) {
                    Expression exp = stack.pop();
                    if (exp instanceof ToInpArg t) {
                        names.add(null);
                        values.add(t.arg);
                    } else {
                        ToMapArg m = (ToMapArg) exp;
                        names.add(m.name);
                        values.add(m.arg);
                    }
                }

                Parameters parameters = callable.getParameters(getCurrentThread());

                if (names.size() > parameters.count()) return;

                LinkedHashMap<String, Expression> finals = new LinkedHashMap<>();

                int index = 0;

                Iterator<Expression> valueItr = values.iterator();
                for (String name : names) {
                    if (name != null) {
                        if (!parameters.hasName(name)) return;
                        boolean success = finals.put(name, valueItr.next()) == null;
                        if (!success) return;
                    } else {
                        String nextParam = parameters.getNames().get(index);
                        boolean success = finals.put(nextParam, valueItr.next()) == null;
                        if (!success) return;
                    }
                    index++;
                }

                for (Map.Entry<String, TObject> defaultParam : parameters.toMap().entrySet()) {
                    if (!finals.containsKey(defaultParam.getKey()) && defaultParam.getValue() != null)
                        finals.put(defaultParam.getKey(), fromObject(defaultParam.getValue()));
                }

                if (finals.size() < parameters.count()) return;

                Expression[] flatten = finals.values().toArray(new Expression[0]);

                for (int i = 0; i < flatten.length; i++) {
                    Expression exp = flatten[i];
                    extendFlow(new StoreTemp(exp, i));
                }

                extendFlow(new PushFrame(func, callable instanceof com.tscript.runtime.typing.Type));

                for (int i = 0; i < flatten.length; i++)
                    stack.push(new LoadTemp(i));
            }
            else {
                List<Expression> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) args.add(stack.remove());
                stack.push(new CallInp(func, args));
            }
        }

    }


    @Override
    public void callSuper(byte argCount) {
        com.tscript.runtime.typing.Type superType =
                getCurrentThread().frameStack.element().getOwner().getType().getSuperType();

        if (superType.isVirtual()){
            for (int i = 0; i < argCount; i++)
                extendFlow(new StoreTemp(stack.remove(), i));

            extendFlow(new PushSuperFrame());

            for (int i = 0; i < argCount; i++)
                stack.push(new LoadTemp(i));
        }
        else {
            throw new UnsupportedOperationException("not implemented yet");
        }

        super.callSuper(argCount);
    }

    @Override
    public void toInplaceArgument() {
        super.toInplaceArgument();
        stack.push(new ToInpArg(stack.remove()));
    }

    @Override
    public void toMappedArgument(byte b1, byte b2) {
        super.toMappedArgument(b1, b2);
        stack.push(new ToMapArg(getUtf8Constant(b1, b2), stack.remove()));
    }

    @Override
    public void returnFunction() {
        super.returnFunction();

        if (configuration.isReturnComplete()) {
            completeTrace(stack.remove());
        }
        else {
            extendFlow(new StoreTemp(stack.remove(), 0));
            extendFlow(PopFrame.INSTANCE);
            stack.push(new LoadTemp(0));
        }
    }

    @Override
    public void loadGlobal(byte address) {
        super.loadGlobal(address);
        stack.push(new LoadGlobal(address));
    }

    @Override
    public void storeGlobal(byte address) {
        super.storeGlobal(address);
        extendFlow(new StoreGlobal(address, stack.remove()));
    }

    @Override
    public void loadLocal(byte address) {
        super.loadLocal(address);
        stack.push(new LoadLocal(address));
    }

    @Override
    public void storeLocal(byte address) {
        super.storeLocal(address);
        if (argsLeft > 0) {
            argsLeft--;
            return;
        }
        extendFlow(new StoreLocal(address, stack.remove()));
    }

    @Override
    public void loadExternal(byte b1, byte b2) {
        super.loadExternal(b1, b2);
        stack.push(new LoadExternal(stack.remove(), getUtf8Constant(b1, b2)));
    }

    @Override
    public void storeExternal(byte b1, byte b2) {
        super.storeExternal(b1, b2);
        Expression where = stack.remove();
        Expression what = stack.remove();
        extendFlow(new StoreExternal(getUtf8Constant(b1, b2), where, what));
    }

    @Override
    public void loadInternal(byte b1, byte b2) {
        super.loadInternal(b1, b2);
        stack.push(new LoadInternal(Conversion.from2Bytes(b1, b2)));
    }

    @Override
    public void storeInternal(byte b1, byte b2) {
        super.storeInternal(b1, b2);
        extendFlow(new StoreInternal(Conversion.from2Bytes(b1, b2), stack.remove()));
    }

    @Override
    public void loadStatic(byte b1, byte b2) {
        super.loadStatic(b1, b2);
        stack.push(new LoadStatic(getUtf8Constant(b1, b2)));
    }

    @Override
    public void storeStatic(byte b1, byte b2) {
        super.storeStatic(b1, b2);
        extendFlow(new StoreStatic(getUtf8Constant(b1, b2), stack.remove()));
    }

    @Override
    public void loadSuper(byte b1, byte b2) {
        super.loadSuper(b1, b2);
        stack.push(new LoadSuper(getUtf8Constant(b1, b2)));
    }

    @Override
    public void storeSuper(byte b1, byte b2) {
        super.storeSuper(b1, b2);
        extendFlow(new StoreSuper(getUtf8Constant(b1, b2), stack.remove()));
    }

    @Override
    public void loadType(byte b1, byte b2) {
        super.loadType(b1, b2);
        stack.push(new Type(Conversion.from2Bytes(b1, b2), false));
    }

    @Override
    public void buildType(byte b1, byte b2) {
        super.buildType(b1, b2);
        stack.remove();
        stack.push(new Type(Conversion.from2Bytes(b1, b2), false));
    }

    @Override
    public void getType() {
        super.getType();
        stack.push(new GetType(stack.remove()));
    }

    @Override
    public void containerRead() {
        super.containerRead();
        Expression container = stack.remove();
        Expression key = stack.remove();
        stack.push(new ContainerRead(container, key));
    }

    @Override
    public void containerWrite() {
        super.containerWrite();
        Expression container = stack.remove();
        Expression key = stack.remove();
        Expression value = stack.remove();
        extendFlow(new ContainerWrite(container, key, value, false));
    }

    @Override
    public void newLine(int line) {
        super.newLine(line);
        if (current.getExpr() instanceof NewLine){
            ((FlowNode)current).expression = new NewLine(line);
            return;
        }
        extendFlow(new NewLine(line));
    }

    @Override
    public void binaryOperation(Opcode opcode) {
        super.binaryOperation(opcode);
        Expression rhs = stack.remove();
        Expression lhs = stack.remove();
        stack.push(new BinaryOp(lhs, rhs, opcode));
    }

    @Override
    public void equals(boolean shouldBeEqual) {
        super.equals(shouldBeEqual);
        Expression rhs = stack.remove();
        Expression lhs = stack.remove();
        stack.push(new Equality(lhs, rhs, shouldBeEqual));
    }

    @Override
    public void getIterator() {
        super.getIterator();
        Expression itr = new Itr(stack.remove(), false);
        stack.push(itr);
        extendFlow(itr);
    }

    @Override
    public void iteratorNext() {
        super.iteratorNext();
        // assume itr is on top
        stack.push(ItrNext.INSTANCE);
    }

    @Override
    public void branchIterator(byte b1, byte b2) {
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();
        int currIp = frame.ip;

        super.branchIterator(b1, b2);

        boolean hasNext = (currIp == frame.ip);
        extendFlow(new ItrNextGuard(hasNext, frame.ip-1, frame.sp+1));
        if (!hasNext){
            if (!stack.isEmpty()) {
                stack.remove();
            }
            // else is loop trace
            extendFlow(new Pop(Expression.EMPTY));
        }
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {

        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();
        int currIp = frame.ip;

        if (inCorrectFunction() && configuration.isLoopComplete()){
            completeTrace(stack.remove());
            frame.ip--;
            return;
        }

        super.branch(b1, b2, ifTrue);

        boolean required = !ifTrue && (currIp == frame.ip);

        extendFlow(new BoolGuard(stack.remove(), required, currIp-1, frame.sp+1));
    }

    @Override
    public void jumpTo(byte b1, byte b2) {
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();

        int prevIp = frame.ip;
        super.jumpTo(b1, b2);
        int curIp = frame.ip;

        if (inCorrectFunction() && configuration.isLoopComplete()){
            completeTrace(Expression.EMPTY);
        }
        else if (curIp < prevIp){
            boolean nativeImageExecuted = handleJumpBack(curIp+1);

            if (nativeImageExecuted) {
                // TODO change CallImage(null, List.of()) to RunImage(currIp+1, currIp)
                stack.push(new Pop(new CallImage(null, List.of())));
            }
        }
    }

    private boolean handleJumpBack(int jumpBackAddress) {
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();

        String context = frame.getModule().getCanonicalPath() + ":" + frame.getName();
        NativeImage image = thread.getVM().getJitCompiler().getCache().getPositional(context, jumpBackAddress);

        if (image == null) {
            return false;
        }

        ImageFrame imgFrame = new ImageFrame(frame);
        try {
            TObject res = image.run(thread, imgFrame);
            if (res != null) {
                frame.push(res);
            }
            frame.ip = jumpBackAddress-1;
        }
        catch (FallbackException ignored){
            if (Profiler.DEBUG){
                System.err.println("Fallback exception");
            }
        }

        return true;
    }

    @Override
    public void pop() {
        super.pop();
        extendFlow(new Pop(stack.remove()));
    }

    @Override
    public void dup() {
        super.dup();
        stack.push(stack.element());
    }

    @Override
    public void posivate() {
        super.posivate();
        stack.push(new TypeGuard(stack.remove(), ip(), sp(), TInteger.TYPE, TReal.TYPE));
    }

    @Override
    public void not() {
        super.not();
        Expression top = stack.remove();

        if (top instanceof Bool)
            top = new TypeGuard(top, ip(), sp(), TBoolean.TYPE);
        else
            top = new TypeGuard(top, ip(), sp(), TInteger.TYPE, TReal.TYPE);

        stack.push(new Not(top, false));
    }

    @Override
    public void negate() {
        super.negate();
        stack.push(new Neg(new TypeGuard(stack.remove(), ip(), sp(), TInteger.TYPE, TReal.TYPE), false));
    }

    @Override
    public void importModule(byte b1, byte b2) {
        super.importModule(b1, b2);

        String[] modulePath = getUtf8Constant(b1, b2).split("[.]");
        int index = getCurrentThread().getVM().getSharedModuleLoader().indexOf(modulePath);
        if (index != -1) {
            stack.push(new ImportModule(index));
        }
    }

    @Override
    public void use(byte b1, byte b2) {
        super.use(b1, b2);

        String name = getUtf8Constant(b1, b2);
        Expression expr = stack.remove();
        extendFlow(new Use(name, expr));
    }

    @Override
    public void useMembers() {
        super.useMembers();
        extendFlow(new UseMembers(stack.remove()));
    }

    @Override
    public void extend(byte b1, byte b2) {
        super.extend(b1, b2);

        Expression what = stack.remove();
        Expression top = stack.remove();
        String name = getUtf8Constant(b1, b2);

        stack.push(new Extended(top, name, what));
    }

    @Override
    public void loadName(byte b1, byte b2) {
        super.loadName(b1, b2);
        stack.push(new LoadName(getUtf8Constant(b1, b2)));
    }

    private boolean inCorrectFunction(){
        return getCurrentThread().frameStack.size() == callStackDepth;
    }

    private void extendFlow(Expression exp) {
        TThread thread = getCurrentThread();

        if (traceSize + 1 >= MAX_TRACE_LIMIT || thread.frameStack.size() < callStackDepth){
            thread.setInterpreter(profiler);
            return;
        }

        if (configuration.isReturnComplete()){
            completeTrace(exp);
            return;
        }

        traceSize++;

        FlowNode next = new FlowNode(exp);
        current.setNext(next);
        current = next;
    }

    private void completeTrace(Expression exp) {
        if (configuration.doesReturn())
            current.setNext(new EndNode(new Return(exp)));
        TThread thread = getCurrentThread();

        JITTask task = configuration.getCompletionTask(root);
        JITCompiler jit = thread.getVM().getJitCompiler();
        jit.handle(task);

        thread.setInterpreter(profiler);
    }

    private TObject top(){
        TThread thread = getCurrentThread();
        if (thread.frameStack.isEmpty()) return null;
        Frame frame = thread.frameStack.element();
        return frame.stack[frame.sp-1];
    }

    private String getUtf8Constant(byte b1, byte b2){
        Module module = getCurrentThread().frameStack.element().getModule();
        Pool pool = module.getPool();
        return pool.loadName(b1, b2);
    }

    private int sp(){
        return getCurrentThread().frameStack.element().sp;
    }

    private int ip(){
        return getCurrentThread().frameStack.element().ip;
    }

}
