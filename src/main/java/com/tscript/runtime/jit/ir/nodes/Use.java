package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Use implements IRNode {

    public IRNode value;
    public String name;

    public Use(IRNode value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitUse(this, p);
    }
}
