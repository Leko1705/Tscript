package com.tscript.runtime.jit.optim;

import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Removes unnecessary function calls that might have been already inlined
 * or do not affect the execution.
 */
public class TrivialCallRemover implements Optimizer {

    private boolean optimized;

    private final Deque<Node> framePushPredecessorsStack = new LinkedList<>();
    private Node prev;

    @Override
    public Node optimize(Node graph) {
        optimized = false;

        Trav trav = new Trav();
        Handler handler = new Handler();

        Context context = new Context(graph);

        while (context.getNext() != null){
            Node current = context.getNext();
            Expression expr = current.getExpr();

            if (!isStateChangingExpr(expr) && !framePushPredecessorsStack.isEmpty()) {
                framePushPredecessorsStack.remove();
                framePushPredecessorsStack.push(null);
            }

            expr.accept(handler);
            prev = current;
            current.step(trav, context);
        }

        return graph;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }


    private static boolean isStateChangingExpr(Expression expr) {
        return expr instanceof NewLine
                || expr instanceof PushFrame
                || expr instanceof PopFrame
                || expr instanceof PushSuperFrame;
    }

    private static class Trav extends TreeTraverseStrategy {
        @Override
        public boolean isThenFirst(BranchNode node, Context context) {
            return false;
        }
    }


    private class Handler extends ExpressionScanner<Void, Void> {

        @Override
        public Void visitPushFrame(PushFrame expr, Void unused) {
            if (expr.isTypeInit){
                framePushPredecessorsStack.push(null);
            }
            else {
                framePushPredecessorsStack.push(prev);
            }
            return null;
        }

        @Override
        public Void visitPushSuperFrame(PushSuperFrame expr, Void unused) {
            // cannot optimize super call away due to super obj binding
            framePushPredecessorsStack.push(null);
            return null;
        }

        @Override
        public Void visitPopFrame(PopFrame expr, Void unused) {
            Node removable = framePushPredecessorsStack.remove();
            if (removable != null) {
                removable.setNext(prev.getNext().getNext());
                optimized = true;
            }
            return null;
        }
    }

}
