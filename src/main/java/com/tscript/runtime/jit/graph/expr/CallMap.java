package com.tscript.runtime.jit.graph.expr;

import java.util.List;

public class CallMap implements Expression {

    public Expression called;
    public final List<Expression> args;
    public boolean canFail = true;

    public CallMap(Expression called, List<Expression> args) {
        this.called = called;
        this.args = args;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (tree == called) {
            called = toReplace;
        }
        args.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitCallMap(this, p);
    }
}
