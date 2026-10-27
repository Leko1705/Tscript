package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.Objects;

public class Equals extends BinaryOp {

    public Equals(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitEquals(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " == " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        IRType lhsTy = tool.type(lhs);
        IRType rhsTy = tool.type(rhs);

        if (lhsTy.isDisjoinedFrom(rhsTy)) {
            return tool.builder().bool(false);
        }

        if (lhs == rhs) {
            return tool.builder().bool(true);
        }

        if (lhs instanceof Literal<?> l && rhs instanceof Literal<?> r) {
            if (Objects.equals(l.getValue(), r.getValue())) {
                return tool.builder().bool(true);
            }
        }

        if (lhs instanceof Type t1 &&  rhs instanceof Type t2) {
            return tool.builder().bool(t1.getAddress() == t2.getAddress());
        }

        if (lhs instanceof Builtin b1 && rhs instanceof Builtin b2) {
            return tool.builder().bool(b1.getAddress() == b2.getAddress());
        }

        if (lhs instanceof Parameter p1 &&  rhs instanceof Parameter p2) {
            return tool.builder().bool(p1.index == p2.index);
        }

        if (lhs instanceof Range r1 && rhs instanceof Range r2) {
            if (r1.getFrom() instanceof Int lhs1
                    && r1.getTo() instanceof Int lhs2
                    && r2.getFrom() instanceof Int rhs1
                    && r2.getTo() instanceof Int rhs2) {
                return tool.builder().bool(
                        Objects.equals(lhs1.value, rhs1.value)
                                && Objects.equals(lhs2.value, rhs2.value));
            }
        }

        return this;
    }
}
