package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.ParseProcess;
import com.tscript.runtime.jit.JitFunctionRunner;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;

import java.util.*;

public class Tracer extends InterpreterDecorator {

    private final Interpreter profiler;
    private final int anchorIp;
    private final Object imageKey;
    private final int frameStackSize;


    private final ParseProcess compProcess;
    private final Graph graph;
    private final Graph.Builder builder;
    
    private IRControl currentControl;

    private IREffect currentEffect;

    private int parametersLeft;

    private final Map<Integer, IRValue> initialLocals;


    public Tracer(Interpreter interpreter,
                  Interpreter profiler,
                  Object imageKey,
                  int ip,
                  int frameStackSize,
                  int parameterCount,
                  ParseProcess compPc) {
        super(interpreter);
        this.profiler = profiler;

        compProcess = compPc;
        graph = compPc.getGraph();
        builder = graph.builder();

        this.currentControl = graph.startControl();
        this.currentEffect = graph.startEffect();

        this.anchorIp = ip;
        this.imageKey = imageKey;
        this.frameStackSize = frameStackSize;

        this.parametersLeft = parameterCount;

        this.initialLocals = new HashMap<>();
    }


    @Override
    public void pushNull() {
        builder.push(builder.nullValue());
        super.pushNull();
    }

    @Override
    public void pushInt(byte value) {
        builder.push(builder.integer(value));
        super.pushInt(value);
    }

    @Override
    public void pushBool(byte value) {
        builder.push(builder.bool(value == 1));
        super.pushBool(value);
    }

    @Override
    public void makeArray(byte size) {
        List<IRValue> exps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            exps.add(builder.pop());
        }
        builder.push(builder.array(exps));
        super.makeArray(size);
    }

    @Override
    public void makeDict(byte size) {
        List<IRValue> keys = new ArrayList<>();
        List<IRValue> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IRValue value = builder.pop();
            IRValue key = builder.pop();
            keys.add(key);
            values.add(value);
        }
        this.builder.push(builder.dict(keys, values));
        super.makeDict(size);
    }

    @Override
    public void makeRange() {
        IRValue to = builder.pop();
        IRValue from = builder.pop();
        builder.push(builder.range(from, to));
        super.makeRange();
    }

    @Override
    public void loadConst(byte b1, byte b2) {
        super.loadConst(b1, b2);
        TObject top = top();
        if (top == null) return;
        IRValue value = fromObject(top);
        builder.push(value);
    }

    @Override
    public void pushThis() {
        builder.push(builder.thisValue());
        super.pushThis();
    }

    @Override
    public void binaryOperation(Opcode opcode) {

        IRValue rhs = builder.pop();
        typeGuard(rhs, Objects.requireNonNull(top(0)));

        IRValue lhs = builder.pop();
        typeGuard(lhs, Objects.requireNonNull(top(1)));

        IRValue op = switch (opcode) {
            case ADD -> builder.add(lhs, rhs);
            case SUB -> builder.sub(lhs, rhs);
            case MUL -> builder.mul(lhs, rhs);
            case DIV -> builder.div(lhs, rhs);
            case MOD -> builder.mod(lhs, rhs);
            case POW -> builder.pow(lhs, rhs);
            case AND -> builder.and(lhs, rhs);
            case OR -> builder.or(lhs, rhs);
            case XOR -> builder.xor(lhs, rhs);
            case IDIV -> builder.idiv(lhs, rhs);
            case SHL -> builder.shl(lhs, rhs);
            case SHR -> builder.shr(lhs, rhs);
            case USHR -> builder.ushr(lhs, rhs);
            case GT -> builder.gt(lhs, rhs);
            case LT -> builder.lt(lhs, rhs);
            default -> throw new AssertionError("Unknown opcode: " + opcode);
        };

        builder.push(op);

        super.binaryOperation(opcode);
    }

    @Override
    public void equals(boolean shouldBeEqual) {

        IRValue lhs = builder.pop();
        typeGuard(lhs, Objects.requireNonNull(top(0)));

        IRValue rhs = builder.pop();
        typeGuard(rhs, Objects.requireNonNull(top(1)));

        if (shouldBeEqual) {
            builder.push(builder.equals(lhs, rhs));
        }
        else {
            builder.push(builder.not(builder.equals(lhs, rhs)));
        }

        super.equals(shouldBeEqual);
    }

    @Override
    public void not() {
        IRValue value = builder.pop();
        typeGuard(value, top());
        builder.push(builder.not(value));
        super.not();
    }

    @Override
    public void negate() {
        IRValue value = builder.pop();
        typeGuard(value, top());
        builder.push(builder.neg(value));
        super.negate();
    }

    @Override
    public void posivate() {
        IRValue value = builder.pop();
        typeGuard(value, top());
        builder.push(builder.pos(value));
        super.posivate();
    }

    @Override
    public void loadBuiltin(byte b1, byte b2) {
        int index = Conversion.from2Bytes(b1, b2);
        builder.push(builder.builtin(index));
        super.loadBuiltin(b1, b2);
    }

    @Override
    public void loadLocal(byte address) {
        builder.push(builder.loadLocal(address));
        super.loadLocal(address);
    }

    @Override
    public void storeLocal(byte address) {
        IRValue value;

        if (parametersLeft > 0) {
            parametersLeft--;
            value = builder.parameter(address);
            graph.typeMap().putPersistent(value, IRType.from(top(), true));
            if (!builder.isDefinedLocal(address)) {
                initialLocals.put((int)address, value);
            }
        }
        else {
            value = builder.pop();
            if (!builder.isDefinedLocal(address)) {
                initialLocals.put((int)address, builder.loadLocal(address));
            }
        }

        builder.storeLocal(address, value);
        super.storeLocal(address);
    }

    @Override
    public void loadGlobal(byte address) {
        TThread thread = getCurrentThread();
        VirtualFrame frame = thread.topVirtualFrame();
        Module module = frame.getModule();
        Member member = module.loadMember(address);
        frame.push(member.get());
        builder.push(builder.loadGlobal(address, member.isMutable()));
    }

    @Override
    public void pop() {
        IRValue value = builder.pop();
        currentEffect = builder.valueEffect(value);
        super.pop();
    }

    @Override
    public void callInplace(byte argCount) {
        TObject top = top();

        TObject[] args = new TObject[argCount];
        for (int i = 0; i < argCount; i++) { args[i] = top(i+1); }

        if (top instanceof VirtualFunction func) {
            Object localContext = func.getMetadata();

            IRValue function = builder.peek();
            currentEffect = builder.guardFunctionIdentity(function, func, ip(), sp());
            builder.pop();

            if (localContext.equals(imageKey)) {
                // recursion detected

                int argIdx = 0;
                for (TObject arg : args) {
                    IRValue argVal = builder.pop();
                    typeGuard(argVal, arg.getType());
                    builder.storeLocal(argIdx++, argVal);
                }

                currentEffect = builder.pushFrame(function);
                closeLoop();
                complete();
            }
            else {
                currentEffect = builder.pushFrame(function);
            }
        }
        else if (top instanceof Callable called) {
            IRValue calledIRNode = builder.pop();
            currentEffect = builder.guardFunctionIdentity(calledIRNode, called, ip(), sp());
            List<TObject> argList = Arrays.asList(args);
            List<TObject> missing = getMissingInplaceArgs(called, args.length);
            if (missing != null) {
                argList.addAll(missing);
                List<IRValue> argValues = argList.stream().map(this::fromObject).toList();
                builder.push(builder.call(calledIRNode, argValues));
            }

        }
        
        super.callInplace(argCount);
    }

    private List<TObject> getMissingInplaceArgs(Callable callable, int givenArgCount) {
        Parameters parameters = callable.getParameters(getCurrentThread());
        int lastUnspecifiedParamIdx = parameters.getLastNonDefinedParameterIndex();
        if (givenArgCount <= lastUnspecifiedParamIdx) {
            // a missing argument was not passed
            return null;
        }
        int missingCount = parameters.count() - givenArgCount;
        return parameters.getTail(parameters.count() - missingCount);
    }

    @Override
    public void callMapped(byte argCount) {
        TObject top = top();

        if (top instanceof Callable called) {
            IRValue calledIRNode = builder.pop();
            List<IRValue> args = flatMappedCallArgs(called, argCount);

            if (args != null) {
                builder.push(builder.call(calledIRNode, args));
            }
        }

        super.callMapped(argCount);
    }

    private List<IRValue> flatMappedCallArgs(Callable called, int argCount) {
        List<String> names = new ArrayList<>(argCount);
        List<IRValue> values = new ArrayList<>(argCount);

        for (int i = 0; i < argCount; i++) {
            IRValue exp = builder.pop();
            if (exp instanceof ToInpArg t) {
                names.add(null);
                values.add(t.getValue());
            } else {
                ToMapArg m = (ToMapArg) exp;
                names.add(m.getName());
                values.add(m.getValue());
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

        return finals.values().stream().toList();
    }

    @Override
    public void returnFunction() {
        TThread thread = getCurrentThread();

        if (thread.frameStack.size() == frameStackSize){
            currentControl = builder.returnControl(builder.pop());
            complete();
        }
        else {
            IRValue retVal = builder.pop();
            currentEffect = builder.popFrame();
            builder.push(retVal);
        }

        super.returnFunction();
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {

        boolean jumped = (TNIUtils.isTrue(top()) == ifTrue);

        IRValue condition = builder.pop();
        if (ifTrue != jumped) {
            condition = builder.not(condition);
        }

        currentEffect = builder.guardBool(condition, ip(), sp());


        String localContext = JitFunctionRunner.getLoopKey(getCurrentThread(), ip());

        if (traceClosed(localContext)){
            closeLoop();
            complete();
        }

        super.branch(b1, b2, ifTrue);
    }

    private boolean traceClosed(Object key) {
        return ip() == anchorIp && key.equals(imageKey);
    }

    @Override
    public void newLine(int line) {
        currentEffect = builder.newLine(line);
        super.newLine(line);
    }

    private TObject top(){
        return top(0);
    }

    private TObject top(int offset){
        TThread thread = getCurrentThread();
        if (thread.frameStack.isEmpty()) return null;
        VirtualFrame frame = thread.topVirtualFrame();
        return frame.stack[frame.sp-1-offset];
    }

    private String getUtf8Constant(byte b1, byte b2){
        Module module = getCurrentThread().topVirtualFrame().getModule();
        Pool pool = module.getPool();
        return pool.loadName(b1, b2);
    }

    private IRValue fromObject(TObject obj) {
        if (obj instanceof TString s) return builder.string(s.getValue());
        else if (obj instanceof TReal r) return builder.real(r.getValue());
        else if (obj instanceof TBoolean b) return builder.bool(b.getValue());
        else if (obj instanceof TInteger i) return builder.integer(i.getValue());
        else if (obj == com.tscript.runtime.typing.Null.INSTANCE) return builder.nullValue();
        else if (obj instanceof com.tscript.runtime.typing.Range r) 
            return builder.range(
                    builder.integer(r.getFrom()),
                    builder.integer(r.getTo())
            );
        else if (obj instanceof TArray a) {
            List<IRValue> exps = new ArrayList<>();
            for (TObject entry : a.getValue()){
                exps.add(fromObject(entry));
            }
            return builder.array(exps);
        }
        else if (obj instanceof TDictionary d){
            List<IRValue> keys = new ArrayList<>();
            List<IRValue> values = new ArrayList<>();
            for (Map.Entry<TObject, TObject> entry : d.getValue().entrySet()){
                keys.add(fromObject(entry.getKey()));
                values.add(fromObject(entry.getValue()));
            }
            return builder.dict(keys, values);
        }
        else throw new AssertionError();
    }

    private int sp(){
        return getCurrentThread().topVirtualFrame().sp;
    }

    private int ip(){
        return getCurrentThread().topVirtualFrame().ip - 1;
    }

    private void typeGuard(IRValue given, TObject expectedTypeInstance) {
        currentEffect = builder.guardBool(
                builder.equals(
                        builder.typeof(given),
                        builder.type(expectedTypeInstance.getType())
                ),
                ip(),
                sp()
        );
    }

    private void closeLoop() {
        // "preheader" control/effect: where the loop starts before first iteration
        IRControl preHeaderCtrl = builder.startControl();
        graph.setStartControl(preHeaderCtrl);

        // The "backedge" control/effect: what we have now after tracing one iteration
        IRControl body = this.currentControl;

        LoopHeadNode loopHeadCtrl = new LoopHeadNode(graph, preHeaderCtrl);
        LoopEndNode loopEndCtrl = new LoopEndNode(graph, body, loopHeadCtrl);
        loopHeadCtrl.addBackedge(loopEndCtrl);
        loopHeadCtrl.getSuccessors().add(body);

        for (Map.Entry<Integer, IRValue> preLoopValue : builder.getSsa(body).entrySet()) {
            int address = preLoopValue.getKey();

            IRValue initialNode = preLoopValue.getValue(); // The node created at loop start
            IRValue currentValAtEnd = builder.loadLocal(address); // The node created during iteration

            // Create the Phi: it merges the 'before loop' value and 'end of loop' value
            // Note: Phi nodes sit at LoopHeads
            Phi phi = builder.phi(initialNode, currentValAtEnd);

            // Any node inside the loop that was using the 'initialNode'
            // must now use the 'phi' instead.
            initialNode.replaceAllUsesWith(phi);

            // Re-link: The phi's first input should actually be the OLD initialNode
            // but we just replaced all uses! We must manually set the phi input back.
            phi.getArgs().set(0, initialNode);
            initialNode.getUsers().add(phi);
        }

        IRControl exitControl = builder.startControl();

        this.currentControl = loopHeadCtrl;
    }

    private void complete() {
        compProcess.compile();
        getCurrentThread().setInterpreter(profiler);
    }

}
