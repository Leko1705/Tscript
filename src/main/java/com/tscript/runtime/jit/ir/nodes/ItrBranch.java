package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class ItrBranch implements IRNode {

    public IRBlock jumpBlock;
    public IRBlock noJumpBlock;
    public int address;

    public ItrBranch(IRBlock jumpBlock, IRBlock noJumpBlock, int address) {
        this.jumpBlock = jumpBlock;
        this.noJumpBlock = noJumpBlock;
        this.address = address;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
