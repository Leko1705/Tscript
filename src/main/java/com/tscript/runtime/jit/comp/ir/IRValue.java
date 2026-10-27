package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.stamps.Stamp;
import com.tscript.runtime.jit.comp.ir.stamps.VoidStamp;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.*;

public abstract class IRValue implements IRNode {

    private final Set<IRNode> users;

    final List<IRValue> inputs;

    public Stamp stamp;


    public IRValue() {
        this.users = new HashSet<>();
        this.inputs = new ArrayList<>();
        stamp = VoidStamp.INSTANCE;
    }

    public List<IRValue> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public Set<IRNode> getUsers() {
        return users;
    }

    public abstract <P, R> R accept(IRValueVisitor<P, R> visitor, P p);

    public  <P, R> R accept(IRValueVisitor<P, R> visitor) {
        return accept(visitor, null);
    }

    public boolean isConstant() {
        return false;
    }

    public boolean hasSideEffects() {
        return false;
    }

    public IRValue canonicalize(CanonicalizerTool tool) {
        return this;
    }

    public int valueHashCode() {
        // We use identityHashCode of inputs to ensure we aren't recursively
        // walking the graph (which would be O(N) and could loop).
        int result = getClass().hashCode();
        for (IRValue input : getInputs()) {
            result = 31 * result + System.identityHashCode(input);
        }
        return result;
    }

    public boolean valueEquals(IRValue other) {
        if (this == other) return true;
        if (this.getClass() != other.getClass()) return false;
        List<? extends IRValue> myInputs = this.getInputs();
        List<? extends IRValue> otherInputs = other.getInputs();
        if (myInputs.size() != otherInputs.size()) return false;

        for (int i = 0; i < myInputs.size(); i++) {
            if (myInputs.get(i) != otherInputs.get(i)) return false;
        }
        return true;
    }

    public void unlink() {
        for (IRValue input : this.getInputs()) {
            input.getUsers().remove(this);
        }
    }

    public void replaceAllUsesWith(IRValue replacement) {
        List<IRNode> usersCopy = new ArrayList<>(this.getUsers());
        for (IRNode user : usersCopy) {
            if (user instanceof IRValue vUser) {
                vUser.replaceInput(this, replacement);
            }
        }
    }

    public void replaceInput(IRValue oldInput, IRValue newInput) {
        Collections.replaceAll(this.inputs, oldInput, newInput);
        newInput.getUsers().add(this);
        oldInput.getUsers().remove(this);
        updateSpecificFields(oldInput, newInput);
    }

    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
    }

}
