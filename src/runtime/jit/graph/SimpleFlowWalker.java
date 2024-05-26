package runtime.jit.graph;

public class SimpleFlowWalker<R, P> implements FlowWalker<R, P> {

    private final R defaultValue;

    public SimpleFlowWalker(R defaultValue) {
        this.defaultValue = defaultValue;
    }

    public SimpleFlowWalker() {
        this.defaultValue = null;
    }

    public R defaultAction(P p){
        return defaultValue;
    }

    @Override
    public R visitStartNode(Flow.StartNode startNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadParameterNode(Flow.LoadParameterNode loadParameterNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitMergeNode(Flow.MergeNode mergeNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitExpressionStatementNode(Flow.ExpressionStatementNode statementNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitIfNode(Flow.IfNode ifNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoopBeginNode(Flow.LoopBeginNode loopBeginNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoopEndNode(Flow.LoopEndNode loopEndNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoopExitNode(Flow.LoopExitNode loopExitNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitTryStartNode(Flow.TryStartNode tryStartNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitTryEndNode(Flow.TryEndNode tryEndNode, P p) {
        return null;
    }

    @Override
    public R visitCatchStartNode(Flow.CatchStartNode catchStartNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitCatchEndNode(Flow.CatchEndNode catchEndNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitThrowNode(Flow.ThrowNode throwNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitReturnNode(Flow.ReturnNode returnNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitNewLineNode(Flow.NewLineNode newLineNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitStoreLocalNode(Flow.StoreLocalNode storeLocalNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitStoreStaticNode(Flow.StoreStaticNode storeStaticNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitStoreMemberNode(Flow.StoreMemberNode storeMemberNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitStoreMemberFastNode(Flow.StoreMemberFastNode storeMemberFastNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitContainerWriteNode(Flow.ContainerWriteNode containerWriteNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitBreakPointNode(Flow.BreakPointNode breakPointNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitUseNode(Flow.UseNode useNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitCallSuperNode(Flow.CallSuperNode callSuperNode, P p) {
        return defaultAction(p);
    }

}
