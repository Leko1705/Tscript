package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRControl;
import com.tscript.runtime.jit.comp.ir.IRControlVisitor;
import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.List;

public class Return extends IRControl {

    private IRValue value;


    public Return(IRValue value, IRControl control, Graph graph) {
        super(graph);
        this.value = value;
        value.getUsers().add(this);
        control.getSuccessors().add(this);
        getPredecessors().add(control);

    }

    public IRValue getValue() {
        return value;
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (value == oldInput) {
            value = newInput;
        }
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of(value);
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return  visitor.visitReturn(this, p);
    }
}
