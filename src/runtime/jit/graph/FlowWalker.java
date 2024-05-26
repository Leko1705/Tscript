package runtime.jit.graph;

import static runtime.jit.graph.Flow.*;

public interface FlowWalker<R, P> {

    R visitStartNode(StartNode startNode, P p);

    R visitLoadParameterNode(LoadParameterNode loadParameterNode, P p);

    R visitMergeNode(MergeNode mergeNode, P p);

    R visitExpressionStatementNode(ExpressionStatementNode statementNode, P p);

    R visitIfNode(IfNode ifNode, P p);

    R visitLoopBeginNode(LoopBeginNode loopBeginNode, P p);

    R visitLoopEndNode(LoopEndNode loopEndNode, P p);

    R visitLoopExitNode(LoopExitNode loopExitNode, P p);

    R visitTryStartNode(TryStartNode tryStartNode, P p);

    R visitTryEndNode(TryEndNode tryEndNode, P p);

    R visitCatchStartNode(CatchStartNode catchStartNode, P p);

    R visitCatchEndNode(CatchEndNode catchEndNode, P p);

    R visitThrowNode(ThrowNode throwNode, P p);

    R visitReturnNode(ReturnNode returnNode, P p);

    R visitNewLineNode(NewLineNode newLineNode, P p);

    R visitStoreLocalNode(StoreLocalNode storeLocalNode, P p);

    R visitStoreStaticNode(StoreStaticNode storeStaticNode, P p);

    R visitStoreMemberNode(StoreMemberNode storeMemberNode, P p);

    R visitStoreMemberFastNode(StoreMemberFastNode storeMemberFastNode, P p);

    R visitContainerWriteNode(ContainerWriteNode containerWriteNode, P p);

    R visitBreakPointNode(BreakPointNode breakPointNode, P p);

    R visitUseNode(UseNode useNode, P p);

    R visitCallSuperNode(CallSuperNode callSuperNode, P p);
}
