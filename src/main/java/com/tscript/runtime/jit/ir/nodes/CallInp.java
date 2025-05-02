package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

import java.util.List;

public class CallInp implements IRNode {

    public IRNode called;
    public final List<IRNode> args;

    public CallInp(IRNode called, List<IRNode> args) {
        this.called = called;
        this.args = args;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
