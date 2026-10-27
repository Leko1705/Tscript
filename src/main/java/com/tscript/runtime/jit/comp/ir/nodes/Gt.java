package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Gt extends BinaryOp {

    public Gt(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitGt(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " > " + getRight().toString() + ")";
    }

    // Inside Gt.java
    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (tool.isNumeric(lhs) && tool.isNumeric(rhs) && lhs.valueEquals(rhs)) {
            return tool.builder().bool(false);
        }

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().bool(l.value > r.value);
        }
        if (lhs instanceof Int l && rhs instanceof Real r) {
            return tool.builder().bool(l.value > r.value);
        }
        if (lhs instanceof Real l && rhs instanceof Int r) {
            return tool.builder().bool(l.value > r.value);
        }
        if (lhs instanceof Real l && rhs instanceof Real r) {
            return tool.builder().bool(l.value > r.value);
        }

        // 2. Mirroring: Turn x > y into y < x
        // This reduces the number of Gt nodes in the graph
        return tool.builder().lt(rhs, lhs);
    }
}
