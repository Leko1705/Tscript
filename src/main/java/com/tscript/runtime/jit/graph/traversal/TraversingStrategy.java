package com.tscript.runtime.jit.graph.traversal;

import com.tscript.runtime.jit.graph.*;

public interface TraversingStrategy {

    void onFlowNode(FlowNode node, Context context);

    void onBranchNode(BranchNode node, Context context);

    void onMergeNode(MergeNode node, Context context);

    void onEndNode(EndNode node, Context context);

}
