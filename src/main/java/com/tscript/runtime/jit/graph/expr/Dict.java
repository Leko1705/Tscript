package com.tscript.runtime.jit.graph.expr;

import java.util.ArrayList;
import java.util.List;

public class Dict implements Expression {

    public final List<Expression> keys = new ArrayList<>();
    public final List<Expression> values = new ArrayList<>();

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitDict(this, p);
    }
}
