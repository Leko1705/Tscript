package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Xor extends BinaryOp {

    public Xor(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitXor(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().integer(l.value ^ r.value);
        }
        if (lhs instanceof Bool l && rhs instanceof Bool r) {
            return tool.builder().bool(l.value ^ r.value);
        }
        if (tool.isBool(lhs) && tool.isBooleanOf(rhs, false)) return lhs;
        if (tool.isBool(rhs) && tool.isBooleanOf(lhs, false)) return rhs;
        return this;
    }
}
