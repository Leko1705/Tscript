package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class ItrHasNext implements IRNode {

    public boolean shouldHaveNext;

    public ItrHasNext(boolean shouldHaveNext) {
        this.shouldHaveNext = shouldHaveNext;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
