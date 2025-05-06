package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class PushFrame implements IRNode {

    public IRValue called;
    public int extraLocals = 0;

    public PushFrame(IRValue called) {
        this.called = called;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitPushFrame(this, p);
    }
}
