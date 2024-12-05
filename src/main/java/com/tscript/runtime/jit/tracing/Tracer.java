package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
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
import java.util.concurrent.CompletableFuture;

public class Tracer extends InterpreterDecorator {

    private static final int MAX_TRACE_LIMIT = 1000;

    private final Profiler profiler;
    private final Profiler.Value value;
    private final int callStackDepth;
    private final FlowNode root;
    private Node current;
    private int traceSize;

    private final Deque<Expression> stack;
    private int argsLeft;

    public Tracer(Interpreter interpreter,
                  Profiler profiler,
                  int callStackDepth,
                  Profiler.Value value,
                  int args) {
        super(interpreter);
        this.profiler = profiler;
        this.callStackDepth = callStackDepth;
        this.value = value;
        root = new FlowNode(Expression.EMPTY);
        root.next = root;

        current = root;

        stack = new ArrayDeque<>();
        traceSize = 0;
        this.argsLeft = args;
    }

    @Override
    public void pushNull() {
        super.pushNull();
        stack.push(Null.INSTANCE);
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
        else if (obj == com.tscript.runtime.typing.Null.INSTANCE) return Null.INSTANCE;
        else if (obj instanceof com.tscript.runtime.typing.Range r) return new Range(new Int(r.getFrom()), new Int(r.getTo()));
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
        stack.push(new Range(stack.remove(), stack.remove()));
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
    public void callInplace(byte argCount) {

        Frame frame = getCurrentThread().frameStack.element();
        TObject top = frame.pop();
        frame.push(top);

        super.callInplace(argCount);

        if (top instanceof Callable callable) {
            Expression func = stack.remove();

            if (callable.isVirtual()) {
                stack.push(Expression.EMPTY);
                extendFlow(new PushFrame(func));
                Expression[] args = new Expression[argCount];
                for (int i = 0; i < argCount; i++) args[i] = stack.remove();
                for (int i = argCount - 1; i >= 0; i--) stack.push(args[i]);
            } else {
                List<Expression> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) args.add(stack.remove());
                stack.push(new CallInp(func, args));
            }
        }
    }

    @Override
    public void callMapped(byte argCount) {
        Frame frame = getCurrentThread().frameStack.element();
        TObject top = frame.pop();
        frame.push(top);

        super.callMapped(argCount);

        if (top instanceof Callable callable) {
            Expression func = stack.remove();

            if (callable.isVirtual()) {
                stack.push(Expression.EMPTY);
                extendFlow(new PushFrame(func));
                Expression[] args = new Expression[argCount];
                for (int i = 0; i < argCount; i++) args[i] = stack.remove();
                for (int i = argCount - 1; i >= 0; i--) stack.push(args[i]);
            } else {
                List<Expression> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) args.add(stack.remove());
                stack.push(new CallMap(func, args));
            }
        }
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

        if (traceComplete()) {
            completeTrace(stack.remove());
        }
        else {
            extendFlow(stack.remove());
            stack.push(Expression.EMPTY);
            extendFlow(PopFrame.INSTANCE);
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
    public void loadName(byte b1, byte b2) {
        super.loadName(b1, b2);
        stack.push(new LoadName(getUtf8Constant(b1, b2)));
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
        extendFlow(new ContainerWrite(container, key, value));
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
        Itr itr = new Itr(stack.remove());
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
        extendFlow(new ItrNextGuard(hasNext));
        if (!hasNext){
            stack.remove();
            extendFlow(new Pop(null));
        }
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();
        int currIp = frame.ip;

        super.branch(b1, b2, ifTrue);

        boolean required = !ifTrue && (currIp == frame.ip);
        extendFlow(new BoolGuard(stack.remove(), required));
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
        stack.push(new TypeGuard(stack.remove(), TInteger.TYPE, TReal.TYPE));
    }

    @Override
    public void not() {
        super.not();
        Expression top = stack.remove();

        if (top instanceof Bool)
            top = new TypeGuard(top, TBoolean.TYPE);
        else
            top = new TypeGuard(top, TInteger.TYPE, TReal.TYPE);

        stack.push(new Not(top));
    }

    @Override
    public void negate() {
        super.negate();
        stack.push(new Neg(new TypeGuard(stack.remove(), TInteger.TYPE, TReal.TYPE)));
    }

    private void extendFlow(Expression exp) {
        TThread thread = getCurrentThread();

        if (traceSize + 1 >= MAX_TRACE_LIMIT || thread.frameStack.size() < callStackDepth){
            thread.setInterpreter(profiler);
            return;
        }

        if (traceComplete()){
            completeTrace(exp);
            return;
        }

        traceSize++;

        FlowNode next = new FlowNode(exp);
        current.setNext(next);
        current = next;
    }

    private boolean traceComplete(){
        TThread thread = getCurrentThread();
        return thread.frameStack.size() == callStackDepth;
    }

    private void completeTrace(Expression exp) {
        current.setNext(new EndNode(exp));
        TThread thread = getCurrentThread();
        JITCompiler jit = thread.getVM().getJitCompiler();
        CompletableFuture<NativeImage> future = jit.compile(root);
        future.thenAccept(img -> value.image = img);
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

}
