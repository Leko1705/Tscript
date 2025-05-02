package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

import java.util.ArrayList;
import java.util.List;

public class Dict implements IRNode {

    public final List<IRNode> keys = new ArrayList<>();
    public final List<IRNode> values = new ArrayList<>();

    public Dict() {}

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
