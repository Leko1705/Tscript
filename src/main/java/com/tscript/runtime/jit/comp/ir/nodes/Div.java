package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Real division. The result is always a real (see ALU: DIV).
 */
public class Div extends BinaryOp {

    public Div(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitDiv(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (tool.isNumericLiteral(lhs) && tool.isNumericLiteral(rhs)) {
            return tool.builder().real(tool.asDouble(lhs) / tool.asDouble(rhs));
        }
        if (tool.isReal(lhs) && (tool.isIntegerOf(rhs, 1) || tool.isRealOf(rhs, 1.0))) {
            return lhs;
        }
        return this;
    }
}
