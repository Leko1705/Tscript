package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import com.tscript.runtime.jit.comp.ir.types.*;

import java.util.Objects;

/**
 * Equality as defined by TNIUtils.areEqual (Object#equals of the runtime objects).
 * Note that an Integer is never equal to a Real, since they are objects of different classes.
 */
public class Equals extends BinaryOp {

    public Equals(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitEquals(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Literal<?> l && rhs instanceof Literal<?> r) {
            return tool.builder().bool(l.getClass() == r.getClass() && Objects.equals(l.value, r.value));
        }

        if (lhs instanceof Type t1 && rhs instanceof Type t2) {
            return tool.builder().bool(t1.index == t2.index);
        }

        IRType lt = tool.type(lhs);
        IRType rt = tool.type(rhs);

        if (tool.isPrimitive(lt) && tool.isPrimitive(rt)) {
            if (lt != rt) return tool.builder().bool(false);
            if (lhs == rhs) return tool.builder().bool(true);
            if (lt == NullType.INSTANCE) return tool.builder().bool(true);
            if (lt == BoolType.INSTANCE) {
                if (tool.isBooleanOf(rhs, true)) return lhs;
                if (tool.isBooleanOf(lhs, true)) return rhs;
                if (tool.isBooleanOf(rhs, false)) return tool.builder().not(lhs);
                if (tool.isBooleanOf(lhs, false)) return tool.builder().not(rhs);
            }
        }

        return this;
    }
}
