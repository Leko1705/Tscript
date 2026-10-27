package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Mul extends BinaryOp {

    public Mul(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitMul(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().integer(l.value * r.value);
        }
        if (tool.isNumericLiteral(lhs) && tool.isNumericLiteral(rhs)) {
            return tool.builder().real(tool.asDouble(lhs) * tool.asDouble(rhs));
        }
        if (tool.isIntegerOf(rhs, 1) && tool.isInt(lhs)) {
            return lhs;
        }
        if (tool.isIntegerOf(lhs, 1) && tool.isInt(rhs)) {
            return rhs;
        }
        if ((tool.isIntegerOf(rhs, 0) && tool.isInt(lhs)) || (tool.isIntegerOf(lhs, 0) && tool.isInt(rhs))) {
            return tool.builder().integer(0);
        }
        return this;
    }
}
