package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Range implements IRNode {

    public IRNode from, to;

    public Range(IRNode from, IRNode to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
