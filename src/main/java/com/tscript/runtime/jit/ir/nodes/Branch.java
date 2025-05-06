package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Branch implements IRNode {

    public IRValue condition;
    public IRBlock jumpBlock;
    public IRBlock noJumpBlock;
    public boolean jumpOnTrue;

    public Branch(IRValue condition, IRBlock jumpBlock, IRBlock noJumpBlock, boolean jumpOnTrue) {
        this.condition = condition;
        this.jumpBlock = jumpBlock;
        this.noJumpBlock = noJumpBlock;
        this.jumpOnTrue = jumpOnTrue;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitBranch(this, p);
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == condition) {
            condition = newNode;
            return true;
        }
        return false;
    }
}
