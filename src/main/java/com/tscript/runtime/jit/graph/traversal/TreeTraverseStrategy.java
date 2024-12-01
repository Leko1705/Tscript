package com.tscript.runtime.jit.graph.traversal;

import com.tscript.runtime.jit.graph.*;

import java.util.HashMap;
import java.util.Map;

public abstract class TreeTraverseStrategy implements TraversingStrategy {

    private final Map<MergeNode, Integer> leftMergeInputs = new HashMap<>();
    
    @Override
    public void onFlowNode(FlowNode node, Context context) {
        context.setNext(node.getNext());
    }

    @Override
    public void onBranchNode(BranchNode node, Context context) {
        Node[] order = new Node[2];

        if (isThenFirst(node, context)){
            order[0] = node.getThen();
            order[1] = node.getElse();
        }
        else {
            order[0] = node.getElse();
            order[1] = node.getThen();
        }

        for (Node next : order){
            context.checkpointTrace.push(node);
            context.setNext(next);
        }
    }

    public abstract boolean isThenFirst(BranchNode node, Context context);

    @Override
    public void onMergeNode(MergeNode node, Context context) {
        if (!leftMergeInputs.containsKey(node)){
            // first path is visited
            leftMergeInputs.put(node, node.getInputs() - 1);
        }

        if (leftMergeInputs.get(node) == 0){
            leftMergeInputs.remove(node);
            context.setNext(node.getNext());
        }
        else {
            int leftPredecessorPaths = leftMergeInputs.get(node);
            leftMergeInputs.replace(node, leftPredecessorPaths - 1);
            Node lastCheckpoint = context.checkpointTrace.remove();
            context.setNext(lastCheckpoint);
        }
    }

    @Override
    public void onEndNode(EndNode node, Context context) {
        if (!context.checkpointTrace.isEmpty()){
            Node lastCheckpoint = context.checkpointTrace.remove();
            context.setNext(lastCheckpoint);
        }
    }
}
