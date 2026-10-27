package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.analysis.Typing;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;
import com.tscript.runtime.jit.tracing.SnapshotShape;
import com.tscript.runtime.typing.Callable;

import java.util.*;

// https://github.com/SeaOfNodes/Simple/blob/main/chapter01/README.md
public class Graph {

    private final Map<GVNEntry, IRValue> values = new HashMap<>();
    
    private final Builder builder;
    private final TypeMap typeMap;
    private final CanonicalizerTool canonicalizerTool;
    
    private IRControl startControl;

    private final IREffect startEffect;


    public Graph() {

        this.startControl = new StartControl(this);
        this.startEffect = new StartEffect(startControl, this);

        this.builder = new Builder();
        this.typeMap = new TypeMap();
        this.canonicalizerTool = new CanonicalizerTool(this);
    }

    public IRControl startControl() {
        return startControl;
    }

    public void setStartControl(IRControl startControl) {
        this.startControl = startControl;
    }

    public IREffect startEffect() {
        return startEffect;
    }

    public List<IREffect> getEffectsOf(IRControl control) {
        IREffect e = startEffect;
        while (e != null && e.control != control)
            e = e.next;
        List<IREffect> effects = new ArrayList<>();
        while (e != null && e.control == control) {
            effects.add(e);
            e = e.next;
        }
        return Collections.unmodifiableList(effects);
    }

    public Collection<IRControl> exitControls() {
        return controls().stream().filter(c -> c.getSuccessors().isEmpty()).toList();
    }
    
    public Builder builder() {
        return builder;
    }

    public TypeMap typeMap() {
        return typeMap;
    }

    private IRValue unique(IRValue value) {
        IRValue simplified = value.canonicalize(canonicalizerTool);
        if (simplified != value) {
            value.unlink();
            // assuming the new simplified node
            // was created by the builder
            // which calls unique() itself
            return unique(simplified);
        }

        GVNEntry key = new GVNEntry(value);
        IRValue existing = values.get(key);

        if (existing != null) {
            value.unlink();
            return existing;
        }
        else {
            values.put(key, value);
            return value;
        }
    }

    public Collection<IRValue> values() {
        return values.values();
    }

    public Set<IRControl> controls() {
        Set<IRControl> visited = new HashSet<>();
        Queue<IRControl> queue = new LinkedList<>();

        queue.add(startControl);

        while (!queue.isEmpty()) {
            IRControl control = queue.remove();
            if (visited.contains(control)) continue;
            visited.add(control);
            queue.addAll(control.getSuccessors());
        }

        return Collections.unmodifiableSet(visited);
    }

    
    public class Builder {

        private IRControl currentControl;
        private IREffect currentEffect;
        private final Map<IRControl, Map<Integer, IRValue>> localSsaPerControl;
        private final Deque<IRValue> stack;

        private Builder() {
            this.currentControl = startControl;
            this.currentEffect = startEffect;
            this.localSsaPerControl = new HashMap<>();
            this.stack = new ArrayDeque<>();
        }


        public void push(IRValue value) {
            stack.push(value);
        }

        public IRValue pop() {
            return stack.remove();
        }

        public IRValue peek() {
            return stack.element();
        }


        public IRValue integer(int value) {
            return unique(new Int(value));
        }

        public IRValue real(double value) {
            return unique(new Real(value));
        }

        public IRValue string(String value) {
            return unique(new Str(value));
        }

        public IRValue bool(boolean value) {
            return unique(new Bool(value));
        }

        public IRValue nullValue() {
            return unique(new NullValue());
        }

        public IRValue range(IRValue from, IRValue to) {
            return unique(new Range(from, to));
        }

        public IRValue array(List<IRValue> values) {
            return unique(new Array(values));
        }

        public IRValue dict(List<IRValue> keys, List<IRValue> values) {
            return unique(new Dict(keys, values));
        }

        public IRValue type(com.tscript.runtime.typing.Type type) {
            return unique(new Type(ObjectCache.putId(type)));
        }

        public IRValue add(IRValue lhs, IRValue rhs) {
            return unique(new Add(lhs, rhs));
        }

        public IRValue sub(IRValue lhs, IRValue rhs) {
            return unique(new Sub(lhs, rhs));
        }

        public IRValue mul(IRValue lhs, IRValue rhs) {
            return unique(new Mul(lhs, rhs));
        }

        public IRValue div(IRValue lhs, IRValue rhs) {
            return unique(new Div(lhs, rhs));
        }

        public IRValue idiv(IRValue lhs, IRValue rhs) {
            return unique(new IDiv(lhs, rhs));
        }

        public IRValue mod(IRValue lhs, IRValue rhs) {
            return unique(new Mod(lhs, rhs));
        }

        public IRValue pow(IRValue lhs, IRValue rhs) {
            return unique(new Pow(lhs, rhs));
        }

        public IRValue and(IRValue lhs, IRValue rhs) {
            return unique(new And(lhs, rhs));
        }

        public IRValue or(IRValue lhs, IRValue rhs) {
            return unique(new Or(lhs, rhs));
       }

        public IRValue xor(IRValue lhs, IRValue rhs) {
            return unique(new Xor(lhs, rhs));
        }

        public IRValue equals(IRValue lhs, IRValue rhs) {
            return unique(new Equals(lhs, rhs));
        }

        public IRValue lt(IRValue lhs, IRValue rhs) {
            return unique(new Lt(lhs, rhs, Graph.this));
        }

        public IRValue gt(IRValue lhs, IRValue rhs) {
            return unique(new Gt(lhs, rhs));
        }

        public IRValue shl(IRValue lhs, IRValue rhs) {
            return unique(new Shl(lhs, rhs));
        }

        public IRValue shr(IRValue lhs, IRValue rhs) {
            return unique(new Shr(lhs, rhs));
        }

        public IRValue ushr(IRValue lhs, IRValue rhs) {
            return unique(new Ushr(lhs, rhs));
        }

        public IRValue not(IRValue value) {
            return unique(new Not(value));
        }

        public IRValue neg(IRValue value) {
            return unique(new Neg(value));
        }

        public IRValue pos(IRValue value) {
            return unique(new Pos(value));
        }

        public IRValue call(IRValue called, List<IRValue> args) {
            return unique(new Call(called, args));
        }

        public IRValue builtin(int address) {
            return unique(new Builtin(address));
        }

        public IRValue thisValue() {
            return unique(new This());
        }

        public IRValue typeof(IRValue value) {
            return unique(new Typeof(value));
        }

        public IRValue parameter(int index) {
            return unique(new Parameter(index));
        }

        public IRValue stringify(IRValue arg) {
            return unique(new ToString(arg));
        }

        public boolean isDefinedLocal(int address) {
            Map<Integer, IRValue> variables = getSsa(currentControl);
            return variables.containsKey(address);
        }

        public IRValue loadLocal(int address) {
            Map<Integer, IRValue> variables = getSsa(currentControl);
            if (variables.containsKey(address)) {
                return variables.get(address);
            }
            else {
                return loadVLocal(address);
            }
        }

        public IRValue loadVLocal(int address) {
            return unique(new Local(address));
        }

        public void storeLocal(int address, IRValue value) {
            getSsa(currentControl).put(address, value);
        }

        public IRValue loadGlobal(int address, boolean mutable) {
            return unique(new Global(address, mutable));
        }

        public Phi phi(IRValue... args) {
            return (Phi) unique(new Phi(args));
        }

        public IRControl merge(IRControl left, IRControl right) {
            IRControl mergeCtrl = new MergeControl(Graph.this, left, right);

            Map<Integer, IRValue> leftSsa = getSsa(left);
            Map<Integer, IRValue> rightSsa = getSsa(right);
            Map<Integer, IRValue> merged = new HashMap<>();

            Set<Integer> allVars = new HashSet<>(leftSsa.keySet());
            allVars.addAll(rightSsa.keySet());

            for (Integer var : allVars) {
                IRValue v1 = leftSsa.get(var);
                IRValue v2 = rightSsa.get(var);

                if (v1 == null) {
                    merged.put(var, v2);
                    continue;
                }

                if (v2 == null) {
                    merged.put(var, v1);
                    continue;
                }

                if (v1 == v2) {
                    merged.put(var, v1);
                    continue;
                }

                IRValue mergedValue = unique(new Phi(v1, v2));
                merged.put(var, mergedValue);
            }

            localSsaPerControl.put(mergeCtrl, merged);
            return mergeCtrl;
        }

        public Map<Integer, IRValue> getSsa(IRControl control) {
            return localSsaPerControl.computeIfAbsent(control, c -> new HashMap<>());
        }

        public IREffect guardBool(IRValue condition, int ip, int sp) {
            return createGuard(ip, sp,
                    (snapshotShape, locals, stack)
                            -> new GuardBool(
                                    condition, snapshotShape,
                                    locals, stack, currentEffect,
                                    currentControl, Graph.this));
        }

        public IREffect guardFunctionIdentity(IRValue value, Callable func, int ip, int sp) {
            int identity = ObjectCache.putId(func.getMetadata());
            return createGuard(ip, sp,
                    (snapshotShape, locals, stack)
                            -> new GuardFunctionIdentity(
                            value, identity, snapshotShape,
                            locals, stack, currentEffect,
                            currentControl, Graph.this));
        }

        private interface GuardFactory {
            Guard create(SnapshotShape snapshotShape, IRValue[] locals, IRValue[] stack);
        }

        private IREffect createGuard(int ip, int sp, GuardFactory factory) {
            Map<Integer, IRValue> knownModifiedLocals = getSsa(currentControl);

            IRValue[] locals = new IRValue[knownModifiedLocals.size()];
            int[] localsMapping = new int[knownModifiedLocals.size()];

            int i = 0;
            for (Map.Entry<Integer, IRValue> entry : knownModifiedLocals.entrySet()) {
                locals[i] = entry.getValue();
                localsMapping[i] = entry.getKey();
                i++;
            }

            IRValue[] stack = new IRValue[this.stack.size()];
            int[] stackMapping = new int[this.stack.size()];

            i = this.stack.size() - 1;
            for (IRValue value : this.stack) {
                stack[i] = value;
                stackMapping[i] = i;
                i--;
            }

            SnapshotShape shape = new SnapshotShape(ip, sp, localsMapping, stackMapping);
            return connectEffects(factory.create(shape, locals, stack));
        }

        public IREffect startEffect() {
            return new StartEffect(currentControl, Graph.this);
        }

        public IREffect valueEffect(IRValue value) {
            return connectEffects(new ValueEffect(value, currentEffect, currentControl, Graph.this));
        }

        public IREffect newLine(int line) {
            return connectEffects(new NewLine(line, currentEffect, currentControl, Graph.this));
        }

        public IREffect pushFrame(IRValue called) {
            return connectEffects(new PushFrame(called, currentEffect, currentControl, Graph.this));
        }

        public IREffect popFrame() {
            return connectEffects(new PopFrame(currentEffect, currentControl, Graph.this));
        }



        private IREffect connectEffects(IREffect next) {
            if (currentEffect.next != null) {
                throw new IllegalStateException("Can not set multiple successors for IREffect");
            }
            currentEffect.insertAfter(next);
            currentEffect = next;
            return next;
        }

        public IRControl startControl() {
            return new StartControl(Graph.this);
        }

        public IRControl returnControl(IRValue value) {
            return new Return(value, currentControl, Graph.this);
        }

        public IRControl ifElseControl(IRValue condition) {
            return new IfElse(currentControl, condition, startControl(), startControl(), Graph.this);
        }

        public void setCurrentControl(IRControl control) {
            currentControl = Objects.requireNonNull(control);
        }

        public IRControl currentControl() {
            return currentControl;
        }
    }


    public static class TypeMap {

        private final Map<IRValue, IRType> typeCache = new HashMap<>();
        private final Map<IRValue, IRType> persistentTypes = new HashMap<>();

        private TypeMap(){
        }

        public IRType getType(IRValue value) {
            if (typeCache.containsKey(value)) {
                return typeCache.get(value);
            }
            return Typing.infer(value, this);
        }

        public IRType put(IRValue value, IRType type) {
            typeCache.put(value, type);
            return type;
        }

        public void putPersistent(IRValue value, IRType type) {
            persistentTypes.put(value, type);
        }

        public IRType remove(IRValue key) {
            return typeCache.remove(key);
        }

        public void flush() {
            typeCache.clear();
            typeCache.putAll(persistentTypes);
        }
    }


    private static final class GVNEntry {
        IRValue node;

        public GVNEntry(IRValue node) {
            this.node = node;
        }

        @Override
        public int hashCode() {
            return node.valueHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GVNEntry other) {
                return node.valueEquals(other.node);
            }
            return false;
        }
    }

}
