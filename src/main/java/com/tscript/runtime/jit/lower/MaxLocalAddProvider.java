package com.tscript.runtime.jit.lower;

import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.ExpressionScanner;
import com.tscript.runtime.jit.graph.expr.LoadLocal;
import com.tscript.runtime.jit.graph.expr.StoreLocal;
import com.tscript.runtime.jit.graph.traversal.Traverser;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;

public class MaxLocalAddProvider extends TreeTraverseStrategy {


    public static int calculate(Node node){
        MaxLocalAddProvider provider = new MaxLocalAddProvider();
        ExprScanner scanner = new ExprScanner();
        Traverser.traverse(provider, scanner, node);
        return scanner.max;
    }

    private MaxLocalAddProvider() {}

    @Override
    public boolean isThenFirst(BranchNode node, Context context) {
        return false;
    }


    private static class ExprScanner extends ExpressionScanner<Void, Void> {

        private int max;

        private void handle(int local){
            max = Math.max(max, local);
        }

        @Override
        public Void visitLoadLocal(LoadLocal expr, Void unused) {
            handle(expr.index());
            return super.visitLoadLocal(expr, unused);
        }

        @Override
        public Void visitStoreLocal(StoreLocal expr, Void unused) {
            handle(expr.index);
            return super.visitStoreLocal(expr, unused);
        }
    }

}
