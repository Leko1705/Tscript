package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dict extends IRValue {

    private final List<IRValue> keys;
    private final List<IRValue> values;
    private final List<IRValue> all;


    public Dict(List<IRValue> keys, List<IRValue> values) {
        this.keys = keys;
        this.values = values;
        this.all = new ArrayList<>(this.keys);
        this.all.addAll(this.values);
        for (IRValue key : keys) {
            key.getUsers().add(this);
        }
        for (IRValue value : values) {
            value.getUsers().add(this);
        }
    }

    @Override
    public List<IRValue> getInputs() {
        return all;
    }

    public List<IRValue> getKeys() {
        return keys;
    }

    public List<IRValue> getValues() {
        return values;
    }

    public List<IRValue> getAll() {
        return getInputs();
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        Collections.replaceAll(keys, oldInput, newInput);
        Collections.replaceAll(values, oldInput, newInput);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitDict(this, p);
    }

}
