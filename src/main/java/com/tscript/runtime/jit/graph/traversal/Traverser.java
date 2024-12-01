package com.tscript.runtime.jit.graph.traversal;

import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.Expression;
import com.tscript.runtime.jit.graph.expr.ExpressionVisitor;

public class Traverser {

    private Traverser(){
    }

    public static <P, R> void traverse(TraversingStrategy strategy, ExpressionVisitor<P, R> visitor, Node startNode) {
        Context context = new Context(startNode);

        while (context.getNext() != null){
            Node current = context.getNext();
            Expression expr = current.getExpr();
            expr.accept(visitor);
            current.step(strategy, context);
        }
    }


}
