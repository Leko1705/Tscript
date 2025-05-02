package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Guard implements IRNode {

    public IRNode condition;
    public int ipRecover;
    public int spRecover;

    public Guard(IRNode condition, int ipRecover, int spRecover) {
        this.condition = condition;
        this.ipRecover = ipRecover;
        this.spRecover = spRecover;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
