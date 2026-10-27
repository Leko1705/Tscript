package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Add extends BinaryOp {

    public Add(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitAdd(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Str s && rhs instanceof Literal<?> l) {
            return tool.builder().string(s.value + l.displayString());
        }
        if (rhs instanceof Str s && lhs instanceof Literal<?> l) {
            return tool.builder().string(l.displayString() + s.value);
        }
        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().integer(l.value + r.value);
        }
        if (tool.isNumericLiteral(lhs) && tool.isNumericLiteral(rhs)) {
            return tool.builder().real(tool.asDouble(lhs) + tool.asDouble(rhs));
        }
        if (tool.isIntegerOf(rhs, 0) && tool.isInt(lhs)) {
            return lhs;
        }
        if (tool.isIntegerOf(lhs, 0) && tool.isInt(rhs)) {
            return rhs;
        }
        if (tool.isStringOf(rhs, "") && tool.isStr(lhs)) {
            return lhs;
        }
        if (tool.isStringOf(lhs, "") && tool.isStr(rhs)) {
            return rhs;
        }
        return this;
    }
}
