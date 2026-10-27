package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Lt extends BinaryOp {

    public Lt(IRValue left, IRValue right, Graph graph) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitLt(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " < " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs.valueEquals(rhs)) {
            return tool.builder().bool(false);
        }

        if (lhs instanceof Int l && rhs instanceof Int r) {
            return tool.builder().bool(l.value < r.value);
        }
        if (lhs instanceof Int l && rhs instanceof Real r) {
            return tool.builder().bool(l.value < r.value);
        }
        if (lhs instanceof Real l && rhs instanceof Int r) {
            return tool.builder().bool(l.value < r.value);
        }
        if (lhs instanceof Real l && rhs instanceof Real r) {
            return tool.builder().bool(l.value < r.value);
        }

        return this;
    }
}
