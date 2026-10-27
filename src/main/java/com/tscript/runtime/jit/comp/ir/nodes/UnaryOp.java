package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public abstract class UnaryOp extends IRValue {

    protected UnaryOp(IRValue value) {
        super(value);
    }

    public IRValue getValue() {
        return input(0);
    }
}
