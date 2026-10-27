package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Allocates a new dictionary. Inputs are key0, value0, key1, value1, ...
 */
public class Dict extends IRValue {

    public Dict(List<IRValue> keys, List<IRValue> values) {
        super(interleave(keys, values));
    }

    private static List<IRValue> interleave(List<IRValue> keys, List<IRValue> values) {
        List<IRValue> all = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            all.add(keys.get(i));
            all.add(values.get(i));
        }
        return all;
    }

    public int size() {
        return inputCount() / 2;
    }

    public IRValue getKey(int i) {
        return input(2 * i);
    }

    public IRValue getValue(int i) {
        return input(2 * i + 1);
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitDict(this, p);
    }
}
