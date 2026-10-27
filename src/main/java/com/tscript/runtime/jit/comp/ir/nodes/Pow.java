package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Pow extends BinaryOp {

    public Pow(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitPow(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " ^ " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().real(Math.pow(s.value, l.value));
        }
        if (lhs instanceof Int s && rhs instanceof Real l) {
            return tool.builder().real(Math.pow(s.value, l.value));
        }
        if (lhs instanceof Real s && rhs instanceof Int l) {
            return tool.builder().real(Math.pow(s.value, l.value));
        }
        if (lhs instanceof Real s && rhs instanceof Real l) {
            return tool.builder().real(Math.pow(s.value, l.value));
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().pow(rhs, lhs);
        }

        boolean baseIsZero = (tool.isIntegerOf(lhs, 0) || tool.isRealOf(lhs, 0.0));
        boolean expIsZero = (tool.isIntegerOf(rhs, 0) || tool.isRealOf(rhs, 0.0));

        if (baseIsZero && !expIsZero && tool.isNumeric(rhs)) {
            return tool.builder().real(0.0);
        }
        if (!baseIsZero && expIsZero && tool.isNumeric(lhs)) {
            return tool.builder().real(1.0);
        }

        boolean baseIsOne = (tool.isIntegerOf(lhs, 1) || tool.isRealOf(lhs, 1.0));

        if (baseIsOne && tool.isNumeric(rhs)) {
            return tool.builder().real(1.0);
        }

        boolean expIsOne = (tool.isIntegerOf(rhs, 1) || tool.isRealOf(rhs, 1.0));

        if (expIsOne && tool.isNumeric(lhs)) {
            return lhs;
        }

        return this;
    }
}
