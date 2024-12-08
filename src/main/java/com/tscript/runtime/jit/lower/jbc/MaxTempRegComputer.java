package com.tscript.runtime.jit.lower.jbc;

import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.ExpressionScanner;
import com.tscript.runtime.jit.graph.expr.LoadTemp;
import com.tscript.runtime.jit.graph.expr.StoreTemp;
import com.tscript.runtime.jit.graph.traversal.Traverser;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;

import java.util.concurrent.atomic.AtomicInteger;

public class MaxTempRegComputer {

    public static int compute(Node graph){

        AtomicInteger count = new AtomicInteger(0);

        Traverser.traverse(new TreeTraverseStrategy() {
            @Override
            public boolean isThenFirst(BranchNode node, Context context) {
                return false;
            }
        }, new ExpressionScanner<>(){
            @Override
            public Object visitLoadTemp(LoadTemp expr, Object o) {
                count.set(Math.max(count.get(), expr.index));
                return super.visitLoadTemp(expr, o);
            }

            @Override
            public Object visitStoreTemp(StoreTemp expr, Object o) {
                count.set(Math.max(count.get(), expr.index));
                return super.visitStoreTemp(expr, o);
            }
        }, graph);

        return count.get() + 1;
    }


}
