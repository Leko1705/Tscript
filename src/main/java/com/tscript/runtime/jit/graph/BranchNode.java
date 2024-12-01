package com.tscript.runtime.jit.graph;

import com.tscript.runtime.jit.graph.expr.Expression;
import com.tscript.runtime.jit.graph.traversal.TraversingStrategy;

public class BranchNode implements Node {

    private final Expression condition;

    private final boolean requireTrue;

    private final Node thenSuccessor;
    private final Node elseSuccessor;

    public BranchNode(Expression condition, boolean requireTrue, Node thenSuccessor, Node elseSuccessor) {
        this.condition = condition;
        this.requireTrue = requireTrue;
        this.thenSuccessor = thenSuccessor;
        this.elseSuccessor = elseSuccessor;
    }

    @Override
    public Expression getExpr() {
        return condition;
    }

    public boolean requireTrue() {
        return requireTrue;
    }

    public Node getThen() {
        return thenSuccessor;
    }

    public Node getElse() {
        return elseSuccessor;
    }

    @Override
    public void step(TraversingStrategy walker, Context context) {
        walker.onBranchNode(this, context);
    }
}
