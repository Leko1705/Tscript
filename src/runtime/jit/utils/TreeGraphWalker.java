package runtime.jit.utils;

import runtime.jit.graph.*;

import java.util.HashMap;
import java.util.Map;

public class TreeGraphWalker<P, R> implements FlowWalker<P, R> {

    private final FlowWalker<P, R> walker;
    
    private final Map<FlowNode, Integer> visited = new HashMap<>();
    
    private R defaultResult;
    
    
    public TreeGraphWalker(FlowWalker<P, R> walker) {
        this(walker, null);
    }
    
    public TreeGraphWalker(FlowWalker<P, R> walker, R defaultResult) {
        this.walker = walker;
        this.defaultResult = defaultResult;
    }

    public void setDefaultResult(R defaultResult) {
        this.defaultResult = defaultResult;
    }

    public R getDefaultResult(P p) {
        return defaultResult;
    }

    private boolean isOutsourced(FlowNode node, int maxVisits){
        
        if (visited.containsKey(node)){
            int count = visited.get(node) - 1;
            
            if (count == 0)
                return true;
            
            visited.put(node, count);
            return false;
        }
        
        visited.put(node, maxVisits);
        return false;
    }
    

    @Override
    public R visitStartNode(Flow.StartNode startNode, P p) {
        if (isOutsourced(startNode, 1)) return getDefaultResult(p);
        return walker.visitStartNode(startNode, p);
    }

    @Override
    public R visitBranchNode(Flow.BranchNode branchNode, P p) {
        if (isOutsourced(branchNode, 1)) return getDefaultResult(p);
        return walker.visitBranchNode(branchNode, p);
    }

    @Override
    public R visitBeginNode(Flow.BeginNode beginNode, P p) {
        if (isOutsourced(beginNode, 1)) return getDefaultResult(p);
        return walker.visitBeginNode(beginNode, p);
    }

    @Override
    public R visitGotoNode(Flow.GotoNode gotoNode, P p) {
        if (isOutsourced(gotoNode, 1)) return getDefaultResult(p);
        return walker.visitGotoNode(gotoNode, p);
    }

    @Override
    public R visitPathEndNode(Flow.PathEndNode pathEndNode, P p) {
        if (isOutsourced(pathEndNode, 2)) return getDefaultResult(p);
        return walker.visitPathEndNode(pathEndNode, p);
    }

    @Override
    public R visitLoadParameterNode(Flow.LoadParameterNode loadParameterNode, P p) {
        if (isOutsourced(loadParameterNode, 1)) return getDefaultResult(p);
        return walker.visitLoadParameterNode(loadParameterNode, p);
    }

    @Override
    public R visitExpressionStatementNode(Flow.ExpressionStatementNode expressionStatementNode, P p) {
        if (isOutsourced(expressionStatementNode, 1)) return getDefaultResult(p);
        return walker.visitExpressionStatementNode(expressionStatementNode, p);
    }

    @Override
    public R visitThrowNode(Flow.ThrowNode throwNode, P p) {
        if (isOutsourced(throwNode, 1)) return getDefaultResult(p);
        return walker.visitThrowNode(throwNode, p);
    }

    @Override
    public R visitTryStartNode(Flow.TryStartNode tryStartNode, P p) {
        if (isOutsourced(tryStartNode, 1)) return getDefaultResult(p);
        return null;
    }

    @Override
    public R visitTryEndNode(Flow.TryEndNode tryEndNode, P p) {
        if (isOutsourced(tryEndNode, 1)) return getDefaultResult(p);
        return null;
    }

    @Override
    public R visitCatchStartNode(Flow.CatchStartNode catchStartNode, P p) {
        if (isOutsourced(catchStartNode, 1)) return getDefaultResult(p);
        return walker.visitCatchStartNode(catchStartNode, p);
    }

    @Override
    public R visitCatchEndNode(Flow.CatchEndNode catchEndNode, P p) {
        if (isOutsourced(catchEndNode, 1)) return getDefaultResult(p);
        return walker.visitCatchEndNode(catchEndNode, p);
    }

    @Override
    public R visitReturnNode(Flow.ReturnNode returnNode, P p) {
        if (isOutsourced(returnNode, 1)) return getDefaultResult(p);
        return walker.visitReturnNode(returnNode, p);
    }

    @Override
    public R visitNewLineNode(Flow.NewLineNode newLineNode, P p) {
        if (isOutsourced(newLineNode, 1)) return getDefaultResult(p);
        return walker.visitNewLineNode(newLineNode, p);
    }

    @Override
    public R visitStoreLocalNode(Flow.StoreLocalNode storeLocalNode, P p) {
        if (isOutsourced(storeLocalNode, 1)) return getDefaultResult(p);
        return walker.visitStoreLocalNode(storeLocalNode, p);
    }

    @Override
    public R visitStoreGlobalNode(Flow.StoreGlobalNode storeGlobalNode, P p) {
        if (isOutsourced(storeGlobalNode, 1)) return getDefaultResult(p);
        return null;
    }

    @Override
    public R visitStoreStaticNode(Flow.StoreStaticNode storeStaticNode, P p) {
        if (isOutsourced(storeStaticNode, 1)) return getDefaultResult(p);
        return walker.visitStoreStaticNode(storeStaticNode, p);
    }

    @Override
    public R visitStoreMemberNode(Flow.StoreMemberNode storeMemberNode, P p) {
        if (isOutsourced(storeMemberNode, 1)) return getDefaultResult(p);
        return walker.visitStoreMemberNode(storeMemberNode, p);
    }

    @Override
    public R visitStoreMemberFastNode(Flow.StoreMemberFastNode storeMemberFastNode, P p) {
        if (isOutsourced(storeMemberFastNode, 1)) return getDefaultResult(p);
        return walker.visitStoreMemberFastNode(storeMemberFastNode, p);
    }

    @Override
    public R visitBreakPointNode(Flow.BreakPointNode breakPointNode, P p) {
        if (isOutsourced(breakPointNode, 1)) return getDefaultResult(p);
        return walker.visitBreakPointNode(breakPointNode, p);
    }

    @Override
    public R visitContainerWriteNode(Flow.ContainerWriteNode containerWriteNode, P p) {
        if (isOutsourced(containerWriteNode, 1)) return getDefaultResult(p);
        return walker.visitContainerWriteNode(containerWriteNode, p);
    }

    @Override
    public R visitUseNode(Flow.UseNode useNode, P p) {
        if (isOutsourced(useNode, 1)) return getDefaultResult(p);
        return walker.visitUseNode(useNode, p);
    }

    @Override
    public R visitCallSuperNode(Flow.CallSuperNode callSuperNode, P p) {
        if (isOutsourced(callSuperNode, 1)) return getDefaultResult(p);
        return walker.visitCallSuperNode(callSuperNode, p);
    }
}
