package com.tscript.runtime.jit.graph.expr;

import java.util.ArrayList;
import java.util.List;

public class Dict implements Expression {

    public final List<Expression> keys = new ArrayList<>();
    public final List<Expression> values = new ArrayList<>();

    @Override
    public void replace(Expression toReplace, Expression tree) {
        keys.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
        values.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitDict(this, p);
    }
}
