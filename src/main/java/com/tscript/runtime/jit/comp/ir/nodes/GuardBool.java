package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.tracing.SnapshotShape;

import java.util.List;

public class GuardBool extends Guard {

    private IRValue value;

    public GuardBool(IRValue value,
                     SnapshotShape snapshotShape,
                     IRValue[] locals,
                     IRValue[] stack,
                     IREffect previous,
                     IRControl control,
                     Graph graph) {
        super(snapshotShape, locals, stack, previous, control, graph);
        this.value = value;
        value.getUsers().add(this);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitGuardBool(this, p);
    }

    public IRValue getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "guard " + getValue().toString();
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        super.updateSpecificFields(oldInput, newInput);
        if (oldInput == value) {
            value = newInput;
        }
    }

    @Override
    public List<IRValue> getInputs() {
        List<IRValue> inputs = (List<IRValue>) super.getInputs();
        inputs.add(value);
        return inputs;
    }
}
