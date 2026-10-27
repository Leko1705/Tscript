package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public abstract class BinaryOp extends IRValue {

    protected BinaryOp(IRValue left, IRValue right) {
        super(left, right);
    }

    public IRValue getLeft() {
        return input(0);
    }

    public IRValue getRight() {
        return input(1);
    }
}
