package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
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
    public String toString() {
        return "(" + getLeft().toString() + " xor " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();


        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().integer(s.value ^ l.value);
        }
        if (lhs instanceof Bool s && rhs instanceof Bool l) {
            return tool.builder().bool(l.value ^ s.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().xor(rhs, lhs);
        }

        return this;
    }
}
