package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.analysis.Typing;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;
import com.tscript.runtime.typing.TObject;

import java.util.*;

/**
 * A sea of nodes graph (https://github.com/SeaOfNodes/Simple).
 * <p>
 * Value nodes float freely and are connected only by their data dependencies, except for fixed
 * nodes which are pinned into the effect chain by an {@link Anchor}. Effects form a linear chain
 * starting at {@link #startEffect()}. Control nodes describe the coarse control flow: a start,
 * loop headers (marked in the effect chain by {@link LoopBegin}) and the exit of the graph
 * (a {@link Return} or a {@link LoopEndNode}).
 */
public class Graph {

    private final Map<GVNEntry, IRValue> valueNumbers = new HashMap<>();

    private final Builder builder;
    private final TypeMap typeMap;
    private final CanonicalizerTool canonicalizerTool;

    private final StartControl startControl;
    private final StartEffect startEffect;
    private final List<LoopHeadNode> loops = new ArrayList<>();

    private IRControl exitControl;

    /** looks up compiled side traces by the deoptimization info of their exit (stitching), may be null */
    private java.util.function.Function<Object, Graph> sideTraces = null;

    public Graph() {
        this.startControl = new StartControl(this);
        this.startEffect = new StartEffect(startControl);
        this.typeMap = new TypeMap();
        this.canonicalizerTool = new CanonicalizerTool(this);
        this.builder = new Builder();
    }

    public StartControl startControl() {
        return startControl;
    }

    public IREffect startEffect() {
        return startEffect;
    }

    /**
     * @return the exit of this graph (a {@link Return} or a {@link LoopEndNode}) or null if the graph is incomplete
     */
    public IRControl exitControl() {
        return exitControl;
    }

    public Collection<IRControl> exitControls() {
        return exitControl == null ? List.of() : List.of(exitControl);
    }

    public void setSideTraces(java.util.function.Function<Object, Graph> sideTraces) {
        this.sideTraces = sideTraces;
    }

    /**
     * @return the graph of the compiled side trace attached to the given exit, or null if there
     * is none or stitching is disabled
     */
    public Graph sideTrace(Object exit) {
        return sideTraces == null || exit == null ? null : sideTraces.apply(exit);
    }

    public List<LoopHeadNode> loops() {
        return Collections.unmodifiableList(loops);
    }

    public Builder builder() {
        return builder;
    }

    public TypeMap typeMap() {
        return typeMap;
    }

    public CanonicalizerTool canonicalizerTool() {
        return canonicalizerTool;
    }

    public List<IREffect> effects() {
        List<IREffect> effects = new ArrayList<>();
        for (IREffect e = startEffect; e != null; e = e.next) effects.add(e);
        return effects;
    }

    public List<IREffect> getEffectsOf(IRControl control) {
        List<IREffect> effects = new ArrayList<>();
        for (IREffect e = startEffect; e != null; e = e.next) {
            if (e.control == control) effects.add(e);
        }
        return effects;
    }

    /**
     * @return all value nodes reachable from effects and controls, inputs before users
     */
    public List<IRValue> values() {
        List<IRValue> order = new ArrayList<>();
        Set<IRValue> visited = new HashSet<>();
        List<IRNode> roots = new ArrayList<>(effects());
        if (exitControl != null) roots.add(exitControl);
        for (LoopHeadNode loop : loops) roots.addAll(loop.getPhis());
        for (IRNode root : roots) {
            if (root instanceof IRValue v) visit(v, visited, order);
            else for (IRValue input : root.getInputs()) visit(input, visited, order);
        }
        return order;
    }

    private static void visit(IRValue root, Set<IRValue> visited, List<IRValue> order) {
        // iterative post order to survive long dependency chains
        Deque<Object[]> stack = new ArrayDeque<>();
        if (!visited.add(root)) return;
        stack.push(new Object[]{root, 0});
        while (!stack.isEmpty()) {
            Object[] top = stack.peek();
            IRValue node = (IRValue) top[0];
            int i = (int) top[1];
            List<IRValue> inputs = node.getInputs();
            if (i < inputs.size()) {
                top[1] = i + 1;
                IRValue input = inputs.get(i);
                if (visited.add(input)) stack.push(new Object[]{input, 0});
            }
            else {
                stack.pop();
                order.add(node);
            }
        }
    }

    /**
     * Rebuilds the value numbering table from the reachable nodes.
     * Must be called after nodes were rewired by optimizations.
     */
    public void rehash() {
        valueNumbers.clear();
        for (IRValue value : values()) {
            if (value.isGVNable() && !value.isDead()) {
                valueNumbers.putIfAbsent(new GVNEntry(value), value);
            }
        }
    }

    private IRValue unique(IRValue value) {
        IRValue simplified = value.canonicalize(canonicalizerTool);
        if (simplified != value) {
            // the simplified node was either created by the builder (and therefore
            // already uniqued) or it is an existing node of the graph
            value.kill();
            return simplified;
        }

        if (value.isFixed()) {
            builder.append(new Anchor(value, builder.currentControl, builder.site));
            return value;
        }

        if (!value.isGVNable()) {
            return value;
        }

        GVNEntry key = new GVNEntry(value);
        IRValue existing = valueNumbers.get(key);

        if (existing != null && !existing.isDead()) {
            value.kill();
            return existing;
        }

        valueNumbers.put(key, value);
        return value;
    }

    public void dump(StringBuilder sb) {
        sb.append("values:\n");
        for (IRValue value : values()) {
            sb.append("  ").append(value).append(" : ").append(typeMap.getType(value)).append('\n');
        }
        sb.append("effects:\n");
        for (IREffect effect : effects()) {
            sb.append("  ").append(effect).append('\n');
        }
        sb.append("exit: ");
        if (exitControl instanceof Return r) sb.append("return #").append(r.getValue().id);
        else if (exitControl instanceof Exit x) sb.append("exit to interpreter ").append(x.getState()).append(' ').append(x.getInputs());
        else if (exitControl instanceof LoopEndNode end) {
            sb.append("loop end -> ip ").append(end.getTarget().ip).append(" phis:");
            for (Phi phi : end.getTarget().getPhis()) sb.append(' ').append(phi);
        }
        else sb.append(exitControl);
        sb.append('\n');
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        dump(sb);
        return sb.toString();
    }


    public class Builder {

        private IRControl currentControl;
        private IREffect currentEffect;
        private int site = -1;

        private Builder() {
            this.currentControl = startControl;
            this.currentEffect = startEffect;
        }

        void append(IREffect effect) {
            currentEffect.insertAfter(effect);
            currentEffect = effect;
        }

        /**
         * Sets the source site (ObjectCache id of a {@link com.tscript.runtime.jit.SourceSite})
         * that is attached to subsequently created effects.
         */
        public void setSite(int site) {
            this.site = site;
        }

        public int site() {
            return site;
        }

        public IRControl currentControl() {
            return currentControl;
        }

        public IREffect currentEffect() {
            return currentEffect;
        }

        /* ---------------- constants ---------------- */

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

        public IRValue constant(TObject object) {
            return unique(new Constant(ObjectCache.putId(object)));
        }

        public IRValue builtin(int address) {
            return unique(new Builtin(address));
        }

        public IRValue type(com.tscript.runtime.typing.Type type) {
            return unique(new Type(ObjectCache.putId(type)));
        }

        /* ---------------- entry values ---------------- */

        public IRValue thisValue() {
            return unique(new This());
        }

        public IRValue local(int index) {
            return unique(new Local(index));
        }

        public IRValue local(int index, IRType type) {
            return unique(new Local(index, type));
        }

        public IRValue stackValue(int index) {
            return unique(new StackValue(index));
        }

        public IRValue stackValue(int index, IRType type) {
            return unique(new StackValue(index, type));
        }

        public IRValue parameter(int index, IRType type) {
            return unique(new Parameter(index, type));
        }

        /* ---------------- operations ---------------- */

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

        public IRValue shl(IRValue lhs, IRValue rhs) {
            return unique(new Shl(lhs, rhs));
        }

        public IRValue shr(IRValue lhs, IRValue rhs) {
            return unique(new Shr(lhs, rhs));
        }

        public IRValue ushr(IRValue lhs, IRValue rhs) {
            return unique(new Ushr(lhs, rhs));
        }

        public IRValue lt(IRValue lhs, IRValue rhs) {
            return unique(new Lt(lhs, rhs));
        }

        public IRValue gt(IRValue lhs, IRValue rhs) {
            return lt(rhs, lhs);
        }

        public IRValue leq(IRValue lhs, IRValue rhs) {
            return unique(new Leq(lhs, rhs));
        }

        public IRValue geq(IRValue lhs, IRValue rhs) {
            return leq(rhs, lhs);
        }

        public IRValue equals(IRValue lhs, IRValue rhs) {
            return unique(new Equals(lhs, rhs));
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

        public IRValue typeof(IRValue value) {
            return unique(new Typeof(value));
        }

        public IRValue isType(IRValue value, IRType type) {
            return unique(new IsType(value, type));
        }

        public IRValue truthy(IRValue value) {
            return unique(new Truthy(value));
        }

        public IRValue isSelf(IRValue value) {
            return unique(new IsSelf(value));
        }

        public IRValue range(IRValue from, IRValue to) {
            return unique(new Range(from, to));
        }

        /* ---------------- fixed values ---------------- */

        public IRValue pi(IRValue value, IRType type) {
            return unique(new Pi(value, type));
        }

        public IRValue array(List<IRValue> values) {
            return unique(new Array(values));
        }

        public IRValue dict(List<IRValue> keys, List<IRValue> values) {
            return unique(new Dict(keys, values));
        }

        public IRValue call(IRValue called, List<IRValue> args) {
            return unique(new Call(called, args, site));
        }

        public IRValue loadGlobal(GlobalRef global) {
            return unique(new Global(global));
        }

        public IRValue isCallee(IRValue value, int metadataId) {
            return unique(new IsCallee(value, metadataId));
        }

        public IRValue functionOwner(IRValue function) {
            return unique(new FunctionOwner(function));
        }

        public IRValue newRangeIterator(IRValue from, IRValue to) {
            return unique(new NewRangeIterator(from, to));
        }

        public IRValue getIterator(IRValue iterable) {
            return unique(new GetIterator(iterable));
        }

        public IRValue iterHasNext(IRValue iterator) {
            return unique(new IterHasNext(iterator));
        }

        public IRValue iterNext(IRValue iterator) {
            return unique(new IterNext(iterator));
        }

        public IRValue containerRead(IRValue container, IRValue key) {
            return unique(new ContainerRead(container, key));
        }

        public IRValue getSuper(IRValue object) {
            return unique(new GetSuper(object));
        }

        public IRValue loadMember(IRValue object, MemberAccess access, int index, String name) {
            return unique(new LoadMember(object, access, index, name));
        }

        /* ---------------- effects ---------------- */

        public GuardBool guard(IRValue condition, FrameState state, List<IRValue> stateValues) {
            GuardBool guard = new GuardBool(condition, state, stateValues, currentControl);
            append(guard);
            return guard;
        }

        public void storeGlobal(GlobalRef global, IRValue value) {
            append(new StoreGlobal(global, value, currentControl, site));
        }

        public void storeMember(IRValue object, IRValue value, MemberAccess access, int index, String name) {
            append(new StoreMember(object, value, access, index, name, currentControl, site));
        }

        public void containerWrite(IRValue container, IRValue key, IRValue value) {
            append(new ContainerWrite(container, key, value, currentControl, site));
        }

        /* ---------------- control ---------------- */

        public LoopHeadNode beginLoop(int ip) {
            LoopHeadNode loop = new LoopHeadNode(Graph.this, currentControl, ip);
            loops.add(loop);
            currentControl = loop;
            append(new LoopBegin(loop));
            return loop;
        }

        public Phi phi(LoopHeadNode loop, IRValue entry) {
            return new Phi(loop, entry);
        }

        public LoopEndNode closeLoop(LoopHeadNode loop) {
            LoopEndNode end = new LoopEndNode(Graph.this, currentControl, loop);
            exitControl = end;
            return end;
        }

        public Exit exit(FrameState state, List<IRValue> stateValues) {
            Exit exit = new Exit(state, stateValues, currentControl, Graph.this);
            exitControl = exit;
            return exit;
        }

        public Return returnValue(IRValue value) {
            Return ret = new Return(value, currentControl, Graph.this);
            exitControl = ret;
            return ret;
        }
    }

    /**
     * Has to be called once the graph was completely built. Removes the phis of loops that
     * were never closed (degenerate loops) and redundant phis.
     */
    public void finish() {
        for (LoopHeadNode loop : loops) {
            for (Phi phi : new ArrayList<>(loop.getPhis())) {
                if (!loop.isClosed()) {
                    replacePhi(phi, phi.getEntry());
                }
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (LoopHeadNode loop : loops) {
                for (Phi phi : new ArrayList<>(loop.getPhis())) {
                    IRValue unique = phi.redundantInput();
                    if (unique != null) {
                        replacePhi(phi, unique);
                        changed = true;
                    }
                }
            }
        }
        typeMap.flush();
    }

    public void replacePhi(Phi phi, IRValue replacement) {
        phi.replaceAllUsesWith(replacement);
        phi.getLoop().getPhis().remove(phi);
        phi.kill();
    }


    /**
     * Caches the inferred types of values. Must be flushed whenever the graph changes.
     */
    public class TypeMap {

        private final Map<IRValue, IRType> cache = new HashMap<>();

        private final Map<Phi, IRType> assumptions = new HashMap<>();
        private final Map<IRValue, IRType> optimisticCache = new HashMap<>();

        private TypeMap() {
        }

        public IRType getType(IRValue value) {
            IRType type = cache.get(value);
            if (type != null) return type;

            if (!assumptions.isEmpty()) {
                type = optimisticCache.get(value);
                if (type != null) return type;
            }

            if (value instanceof Phi phi) {
                type = inferPhi(phi);
            }
            else {
                type = Typing.infer(value, this);
            }

            if (assumptions.isEmpty()) cache.put(value, type);
            else optimisticCache.put(value, type);
            return type;
        }

        private IRType inferPhi(Phi phi) {
            if (!phi.isComplete()) {
                // the type of the loop carried value is unknown until the loop is closed
                return UnknownTypeHolder.UNKNOWN;
            }

            IRType assumed = assumptions.get(phi);
            if (assumed != null) return assumed;

            IRType current = getType(phi.getEntry());
            for (int i = 0; i < 8; i++) {
                assumptions.put(phi, current);
                optimisticCache.clear();
                IRType back = getType(phi.getBackedge());
                IRType joined = join(current, back);
                if (joined.equals(current)) {
                    assumptions.remove(phi);
                    if (assumptions.isEmpty()) optimisticCache.clear();
                    return current;
                }
                current = joined;
            }
            assumptions.remove(phi);
            if (assumptions.isEmpty()) optimisticCache.clear();
            return UnknownTypeHolder.UNKNOWN;
        }

        private IRType join(IRType a, IRType b) {
            if (a.equals(b)) return a;
            if (a == UnknownTypeHolder.UNKNOWN || b == UnknownTypeHolder.UNKNOWN) return UnknownTypeHolder.UNKNOWN;
            return com.tscript.runtime.jit.comp.ir.types.UnionType.mixup(a, b);
        }

        public void flush() {
            cache.clear();
            optimisticCache.clear();
        }
    }

    private static final class UnknownTypeHolder {
        static final IRType UNKNOWN = com.tscript.runtime.jit.comp.ir.types.UnknownType.INSTANCE;
    }


    private static final class GVNEntry {
        private final IRValue node;
        private final int hash;

        GVNEntry(IRValue node) {
            this.node = node;
            this.hash = node.valueHashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GVNEntry other && node.valueEquals(other.node);
        }
    }

}
