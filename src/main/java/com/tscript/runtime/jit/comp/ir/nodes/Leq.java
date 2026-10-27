package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Numeric less-or-equal. Greater-or-equal is expressed as Leq with swapped operands.
 */
public class Leq extends BinaryOp {

    public Leq(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitLeq(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().bool(l.value <= r.value);
        }
        if (tool.isNumericLiteral(lhs) && tool.isNumericLiteral(rhs)) {
            return tool.builder().bool(tool.asDouble(lhs) <= tool.asDouble(rhs));
        }
        if (lhs == rhs && tool.isInt(lhs)) {
            return tool.builder().bool(true);
        }
        return this;
    }
}
