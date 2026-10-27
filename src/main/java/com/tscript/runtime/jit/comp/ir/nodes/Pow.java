package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Power. Integer ^ Integer yields an integer ((int) Math.pow), every other numeric combination a real.
 */
public class Pow extends BinaryOp {

    public Pow(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitPow(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().integer((int) Math.pow(l.value, r.value));
        }
        if (tool.isNumericLiteral(lhs) && tool.isNumericLiteral(rhs)) {
            return tool.builder().real(Math.pow(tool.asDouble(lhs), tool.asDouble(rhs)));
        }
        if (tool.isIntegerOf(rhs, 1) && tool.isInt(lhs)) {
            return lhs;
        }
        return this;
    }
}
