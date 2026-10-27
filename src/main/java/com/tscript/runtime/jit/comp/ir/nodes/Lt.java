package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Numeric less-than. Greater-than is expressed as Lt with swapped operands.
 */
public class Lt extends BinaryOp {

    public Lt(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitLt(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().bool(l.value < r.value);
        }
        if (tool.isNumericLiteral(lhs) && tool.isNumericLiteral(rhs)) {
            return tool.builder().bool(tool.asDouble(lhs) < tool.asDouble(rhs));
        }
        if (lhs == rhs && tool.isInt(lhs)) {
            return tool.builder().bool(false);
        }
        return this;
    }
}
