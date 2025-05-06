package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Goto implements IRNode {

    public IRBlock block;

    public Goto(IRBlock block) {
        this.block = block;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitGoto(this, p);
    }
}
