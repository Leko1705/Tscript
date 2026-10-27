package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.List;

/**
 * A loop phi. The first input is the value on loop entry, the second input (set when the loop
 * is closed) is the value at the end of the loop body.
 */
public class Phi extends IRValue {

    private final LoopHeadNode loop;

    public Phi(LoopHeadNode loop, IRValue entry) {
        super(entry);
        this.loop = loop;
        loop.getPhis().add(this);
    }

    public LoopHeadNode getLoop() {
        return loop;
    }

    public IRValue getEntry() {
        return input(0);
    }

    public IRValue getBackedge() {
        return inputCount() > 1 ? input(1) : null;
    }

    public boolean isComplete() {
        return inputCount() > 1;
    }

    public void setBackedge(IRValue value) {
        if (isComplete()) throw new IllegalStateException("phi already closed");
        addInput(value);
    }

    public List<IRValue> getArgs() {
        return getInputs();
    }

    @Override
    public boolean isGVNable() {
        return false;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitPhi(this, p);
    }

    /**
     * A phi is redundant if all its inputs are the same value or the phi itself.
     * @return the unique input or null if the phi is not redundant
     */
    public IRValue redundantInput() {
        IRValue unique = null;
        for (IRValue input : getInputs()) {
            if (input == this || input == unique) continue;
            if (unique != null) return null;
            unique = input;
        }
        return unique;
    }
}
