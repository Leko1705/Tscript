package com.tscript.runtime.jit.graph.expr;

import com.tscript.runtime.core.Opcode;

public class BinaryOp extends Binary {

    public Opcode opcode;

    public BinaryOp(Expression lhs, Expression rhs, Opcode opcode) {
        super(lhs, rhs);
        this.opcode = opcode;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitBinaryOp(this, p);
    }
}
