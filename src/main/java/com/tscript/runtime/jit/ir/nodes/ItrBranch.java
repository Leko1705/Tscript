package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class ItrBranch implements IRNode {

    public IRBlock jumpBlock;
    public IRBlock noJumpBlock;

    public ItrBranch(IRBlock jumpBlock, IRBlock noJumpBlock) {
        this.jumpBlock = jumpBlock;
        this.noJumpBlock = noJumpBlock;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitItrBranch(this, p);
    }
}
