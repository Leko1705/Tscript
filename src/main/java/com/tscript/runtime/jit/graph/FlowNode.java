package com.tscript.runtime.jit.graph;

import com.tscript.runtime.jit.graph.expr.Expression;
import com.tscript.runtime.jit.graph.traversal.TraversingStrategy;

public class FlowNode implements Node {

    public Expression expression;
    public Node next;

    public FlowNode(Expression expression, Node next) {
        this.expression = expression;
        this.next = next;
    }

    @Override
    public Expression getExpr() {
        return expression;
    }

    @Override
    public void step(TraversingStrategy walker, Context context) {
        walker.onFlowNode(this, context);
    }

}
