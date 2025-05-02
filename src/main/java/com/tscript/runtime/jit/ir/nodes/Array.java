package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

import java.util.List;

public class Array implements IRNode {

    public final List<IRNode> content;

    public Array(List<IRNode> content) {
        this.content = content;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
