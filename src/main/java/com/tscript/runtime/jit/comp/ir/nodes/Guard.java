package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRControl;
import com.tscript.runtime.jit.comp.ir.IREffect;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.tracing.SnapshotShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Guard extends IREffect {

    private final SnapshotShape snapshotShape;
    private final IRValue[] locals;
    private final IRValue[] stack;


    public Guard(SnapshotShape snapshotShape,
                 IRValue[] locals,
                 IRValue[] stack,
                 IREffect previous,
                 IRControl control,
                 Graph graph) {
        super(previous, control, graph);
        this.snapshotShape = snapshotShape;
        this.locals = locals;
        this.stack = stack;
    }

    public List<IRValue> getLocals() {
        return new ArrayList<>(Arrays.asList(this.locals));
    }

    public List<IRValue> getStack() {
        return new ArrayList<>(Arrays.asList(this.stack));
    }

    public SnapshotShape getSnapshotShape() {
        return snapshotShape;
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        for (int i = 0; i < locals.length; i++) {
            if (locals[i] == oldInput) {
                locals[i] = newInput;
            }
        }
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == oldInput) {
                stack[i] = newInput;
            }
        }
    }

    @Override
    public List<IRValue> getInputs() {
        List<IRValue> inputs = getLocals();
        inputs.addAll(getStack());
        return inputs;
    }
}
