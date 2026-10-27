package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

import java.util.List;

public class Range extends IRValue {

    private IRValue from;

    private IRValue to;

    public Range(IRValue from, IRValue to) {
        this.from = from;
        this.to = to;
        from.getUsers().add(this);
        to.getUsers().add(this);
    }

    public IRValue getFrom() {
        return from;
    }

    public IRValue getTo() {
        return to;
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of(getFrom(), getTo());
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (oldInput == from) {
            from = newInput;
        }
        if (oldInput == to) {
            to = newInput;
        }
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitRange(this, p);
    }

    @Override
    public String toString() {
        return  "(" + getFrom().toString() + ":" + getTo().toString() + ")";
    }
}
