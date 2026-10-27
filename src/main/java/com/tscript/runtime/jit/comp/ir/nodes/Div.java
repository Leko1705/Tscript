package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Div extends BinaryOp {

    public Div(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitDiv(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " / " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().real(s.value.doubleValue() / l.value);
        }
        if (lhs instanceof Int s && rhs instanceof Real l) {
            return tool.builder().real(l.value / s.value);
        }
        if (lhs instanceof Real s && rhs instanceof Int l) {
            return tool.builder().real(s.value / l.value);
        }
        if (lhs instanceof Real s && rhs instanceof Real l) {
            return tool.builder().real(l.value / s.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().div(rhs, lhs);
        }

        if (tool.isIntegerOf(lhs, 0) && tool.isNumeric(rhs)) {
            return lhs;
        }
        if (tool.isIntegerOf(rhs, 1) && tool.isNumeric(lhs)) {
            return lhs;
        }
        if (tool.isRealOf(lhs, 0.0) && tool.isNumeric(rhs)) {
            return lhs;
        }
        if (tool.isRealOf(rhs, 1.0) && tool.isNumeric(lhs)) {
            return lhs;
        }

        return this;
    }
}
