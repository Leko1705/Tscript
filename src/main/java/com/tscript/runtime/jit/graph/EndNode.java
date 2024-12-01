package com.tscript.runtime.jit.graph;

import com.tscript.runtime.jit.graph.expr.Expression;
import com.tscript.runtime.jit.graph.traversal.TraversingStrategy;

public class EndNode implements Node {

    private final Expression expr;

    public EndNode(Expression expr) {
        this.expr = expr;
    }

    @Override
    public Expression getExpr() {
        return expr;
    }

    @Override
    public void step(TraversingStrategy walker, Context context) {
        walker.onEndNode(this, context);
    }
}
