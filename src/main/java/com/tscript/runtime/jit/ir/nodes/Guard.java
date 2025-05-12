package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Guard implements IRNode {

    public IRValue condition;
    public int ipRecover;
    public int spRecover;
    public boolean shouldHold;

    public Guard(IRValue condition, int ipRecover, int spRecover, boolean shouldHold) {
        this.condition = condition;
        this.ipRecover = ipRecover;
        this.spRecover = spRecover;
        this.shouldHold = shouldHold;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitGuard(this, p);
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
