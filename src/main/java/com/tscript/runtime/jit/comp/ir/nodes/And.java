package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Bitwise and on integers, logical (non short-circuit) and on booleans.
 */
public class And extends BinaryOp {

    public And(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitAnd(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().integer(l.value & r.value);
        }
        if (lhs instanceof Bool l && rhs instanceof Bool r) {
            return tool.builder().bool(l.value && r.value);
        }
        if (tool.isBool(lhs) && tool.isBool(rhs)) {
            if (tool.isBooleanOf(rhs, true)) return lhs;
            if (tool.isBooleanOf(lhs, true)) return rhs;
            if (tool.isBooleanOf(rhs, false) || tool.isBooleanOf(lhs, false)) return tool.builder().bool(false);
            if (lhs == rhs) return lhs;
        }
        return this;
    }
}
