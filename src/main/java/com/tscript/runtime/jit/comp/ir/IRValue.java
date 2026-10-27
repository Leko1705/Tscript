package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.*;

/**
 * A value node of the sea of nodes graph.
 * <p>
 * All inputs are stored in a single input list owned by this base class, so that
 * {@link #replaceInput(IRValue, IRValue)} works uniformly for every node type.
 * <p>
 * There are two kinds of value nodes:
 * <ul>
 *     <li>floating nodes: pure computations that can be scheduled freely and are value numbered (GVN)</li>
 *     <li>fixed nodes ({@link #isFixed()}): nodes that read or write state or allocate. They are anchored
 *     at a fixed position in the effect chain by an {@link com.tscript.runtime.jit.comp.ir.nodes.Anchor}
 *     and are never value numbered.</li>
 * </ul>
 */
public abstract class IRValue implements IRNode {

    private static int nextId = 0;

    public final int id = nextId++;

    private final Set<IRNode> users = new LinkedHashSet<>();

    private final List<IRValue> inputs;

    private boolean dead = false;

    protected IRValue(IRValue... inputs) {
        this(Arrays.asList(inputs));
    }

    protected IRValue(List<IRValue> inputs) {
        this.inputs = new ArrayList<>(inputs);
        for (IRValue input : this.inputs) {
            input.users.add(this);
        }
    }

    @Override
    public List<IRValue> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    protected IRValue input(int index) {
        return inputs.get(index);
    }

    protected int inputCount() {
        return inputs.size();
    }

    protected void addInput(IRValue value) {
        inputs.add(value);
        value.users.add(this);
    }

    public Set<IRNode> getUsers() {
        return users;
    }

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public abstract <P, R> R accept(IRValueVisitor<P, R> visitor, P p);

    public <P, R> R accept(IRValueVisitor<P, R> visitor) {
        return accept(visitor, null);
    }

    /**
     * @return true if the value is known at compile time and does not depend on any runtime state.
     */
    public boolean isConstant() {
        return false;
    }

    /**
     * @return true if this node must be anchored in the effect chain.
     */
    public boolean isFixed() {
        return false;
    }

    /**
     * @return true if the evaluation of this node may throw (e.g. integer division by zero).
     * Such nodes must not be hoisted out of loops.
     */
    public boolean canTrap() {
        return false;
    }

    /**
     * @return true if this node may take part in global value numbering.
     */
    public boolean isGVNable() {
        return !isFixed();
    }

    public IRValue canonicalize(CanonicalizerTool tool) {
        return this;
    }

    /**
     * Node specific data (besides the inputs) that distinguishes two nodes of the same class.
     */
    protected boolean dataEquals(IRValue other) {
        return true;
    }

    protected int dataHashCode() {
        return 0;
    }

    public final int valueHashCode() {
        int result = getClass().hashCode() * 31 + dataHashCode();
        for (IRValue input : inputs) {
            result = 31 * result + System.identityHashCode(input);
        }
        return result;
    }

    public final boolean valueEquals(IRValue other) {
        if (this == other) return true;
        if (this.getClass() != other.getClass()) return false;
        if (this.inputs.size() != other.inputs.size()) return false;
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i) != other.inputs.get(i)) return false;
        }
        return dataEquals(other);
    }

    public boolean isDead() {
        return dead;
    }

    /**
     * Removes this node from the user lists of its inputs and marks it as dead.
     */
    public void kill() {
        for (IRValue input : inputs) {
            input.users.remove(this);
        }
        dead = true;
    }

    public void replaceAllUsesWith(IRValue replacement) {
        if (replacement == this) return;
        for (IRNode user : new ArrayList<>(users)) {
            user.replaceInput(this, replacement);
        }
    }

    @Override
    public void replaceInput(IRValue oldInput, IRValue newInput) {
        boolean replaced = false;
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i) == oldInput) {
                inputs.set(i, newInput);
                replaced = true;
            }
        }
        if (replaced) {
            oldInput.users.remove(this);
            newInput.users.add(this);
        }
    }

    protected String name() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", name() + "#" + id + "(", ")");
        for (IRValue input : inputs) sj.add("#" + input.id);
        return sj.toString();
    }
}
