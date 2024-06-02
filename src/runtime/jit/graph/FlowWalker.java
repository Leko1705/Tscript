package runtime.jit.graph;

import runtime.jit.graph.Flow.*;

public interface FlowWalker<P, R> {

    R visitStartNode(StartNode startNode, P p);


    R visitBranchNode(BranchNode branchNode, P p);

    R visitBeginNode(BeginNode beginNode, P p);

    R visitGotoNode(GotoNode gotoNode, P p);

    R visitPathEndNode(PathEndNode pathEndNode, P p);
    

    R visitLoadParameterNode(LoadParameterNode loadParameterNode, P p);

    R visitExpressionStatementNode(ExpressionStatementNode expressionStatementNode, P p);

    R visitThrowNode(ThrowNode throwNode, P p);

    R visitTryStartNode(TryStartNode tryStartNode, P p);

    R visitTryEndNode(TryEndNode tryEndNode, P p);

    R visitCatchStartNode(CatchStartNode catchStartNode, P p);

    R visitCatchEndNode(CatchEndNode catchEndNode, P p);

    R visitReturnNode(ReturnNode returnNode, P p);

    R visitNewLineNode(NewLineNode newLineNode, P p);

    R visitStoreLocalNode(StoreLocalNode storeLocalNode, P p);

    R visitStoreGlobalNode(StoreGlobalNode storeGlobalNode, P p);

    R visitStoreStaticNode(StoreStaticNode storeStaticNode, P p);

    R visitStoreMemberNode(StoreMemberNode storeMemberNode, P p);

    R visitStoreMemberFastNode(StoreMemberFastNode storeMemberFastNode, P p);

    R visitBreakPointNode(BreakPointNode breakPointNode, P p);

    R visitContainerWriteNode(ContainerWriteNode containerWriteNode, P p);

    R visitUseNode(UseNode useNode, P p);

    R visitCallSuperNode(CallSuperNode callSuperNode, P p);
}
