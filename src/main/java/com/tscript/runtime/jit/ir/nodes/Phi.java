package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

import java.util.HashMap;
import java.util.Map;

public class Phi implements IRValue {

    public final Map<IRBlock, IRValue> options;

    public Phi() {
        this.options = new HashMap<>();
    }

    @Override
    public IRType getType() {

        if (options.size() == 1) {
            return options.values().iterator().next().getType();
        }

        return IRUnknownType.INSTANCE;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
