package com.tscript.runtime.jit.graph;

import com.tscript.runtime.jit.graph.expr.Expression;
import com.tscript.runtime.jit.graph.traversal.TraversingStrategy;

public class MergeNode implements Node {

    private final int inputs;
    private final Expression expression;
    private Node next;

    public MergeNode(int inputs, Expression expression, Node next) {
        this.inputs = inputs;
        this.expression = expression;
        this.next = next;
    }

    @Override
    public Expression getExpr() {
        return expression;
    }

    @Override
    public void setNext(Node next) {
        this.next = next;
    }

    public Node getNext() {
        return next;
    }

    public int getInputs() {
        return inputs;
    }

    @Override
    public void step(TraversingStrategy walker, Context context) {
        walker.onMergeNode(this, context);
    }

}
