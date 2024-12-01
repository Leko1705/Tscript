package com.tscript.runtime.jit.graph.expr;

import com.tscript.runtime.typing.Type;

public class TypeGuard extends Guard {

    public Type[] required;

    public TypeGuard(Expression expression, Type... types) {
        super(expression);
        this.required = types;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitTypeGuard(this, p);
    }
}
