package com.tscript.runtime.jit.optim;

import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;

import java.util.*;

public class DeadVariableEliminator implements Optimizer {

    private boolean optimized;
    private Node prev;

    private final State<Map<Integer, List<Node>>> removalCandidates = new State<>(new HashMap<>());
    private final Map<Integer, List<Node>> temps = new HashMap<>();
    private final Deque<Boolean> canEliminate = new ArrayDeque<>();

    @Override
    public Node optimize(Node graph) {
        optimized = false;

        Context context = new Context(graph);
        Handler handler = new Handler();
        Trav trav = new Trav();

        while (context.getNext() != null){
            Node current = context.getNext();
            Expression expr = current.getExpr();
            expr.accept(handler);
            prev = current;
            current.step(trav, context);
        }
        removeUnusedLocals();
        removeUnusedTemps();

        return graph;
    }


    private void removeUnusedLocals(){
        for (List<Node> indices : removalCandidates.get().values()) {
            for (Node candidate : indices) {
                candidate.setNext(candidate.getNext().getNext());
                optimized = true;
            }
            indices.clear();
        }
    }

    private void removeUnusedTemps(){
        for (List<Node> indices : temps.values()) {
            for (Node candidate : indices) {
                candidate.setNext(candidate.getNext().getNext());
                optimized = true;
            }
            indices.clear();
        }
    }


    @Override
    public boolean isOptimized() {
        return optimized;
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
            scan(expr.called, null);
            removalCandidates.storeAndSet(new HashMap<>());
            canEliminate.push(!expr.isTypeInit);
            return null;
        }

        @Override
        public Void visitPushSuperFrame(PushSuperFrame expr, Void unused) {
            removalCandidates.storeAndSet(new HashMap<>());
            canEliminate.push(true);
            return null;
        }

        @Override
        public Void visitPopFrame(PopFrame expr, Void unused) {
            removeUnusedLocals();
            removalCandidates.restore();
            canEliminate.remove();
            return null;
        }

        @Override
        public Void visitStoreLocal(StoreLocal expr, Void unused) {
            scan(expr.what, null);

            if (expr.what instanceof LoadLocal l){
                if (expr.index == l.index()){
                    prev.setNext(prev.getNext().getNext());
                    optimized = true;
                }
            }

            int address = expr.index;
            if (!removalCandidates.get().containsKey(address))
                removalCandidates.get().put(address, new LinkedList<>());
            removalCandidates.get().get(address).add(prev);

            return null;
        }

        @Override
        public Void visitLoadLocal(LoadLocal expr, Void unused) {
            int address = expr.index();
            if (!removalCandidates.get().containsKey(address))
                removalCandidates.get().put(address, new LinkedList<>());
            removalCandidates.get().get(expr.index()).clear();
            return null;
        }

        @Override
        public Void visitStoreTemp(StoreTemp expr, Void unused) {
            scan(expr.what, null);

            if (expr.what instanceof LoadTemp l){
                if (expr.index == l.index && canEliminate.element()){
                    prev.setNext(prev.getNext().getNext());
                    optimized = true;
                }
            }
            int address = expr.index;
            if (!temps.containsKey(address))
                temps.put(address, new LinkedList<>());
            temps.get(address).add(prev);

            return null;
        }

        @Override
        public Void visitLoadTemp(LoadTemp expr, Void unused) {
            int address = expr.index;
            if (!temps.containsKey(address))
                temps.put(address, new LinkedList<>());
            temps.get(address).clear();
            return null;
        }

        @Override
        public Void visitStoreGlobal(StoreGlobal expr, Void unused) {
            scan(expr.what, null);

            if (expr.what instanceof LoadGlobal l){
                if (expr.index == l.index()){
                    prev.setNext(prev.getNext().getNext());
                    optimized = true;
                }
            }

            return null;
        }
    }

}
