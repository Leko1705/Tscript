package runtime.jit.optim.techniques;

import runtime.jit.graph.ExpressionVisitor;
import runtime.jit.graph.SimpleFlowWalker;

import static runtime.jit.graph.Expressions.*;


public abstract class Technique<R, P>
        extends SimpleFlowWalker<R, P>
        implements ExpressionVisitor<R, P> {

    private boolean optimizationPerformed = false;

    public boolean optimizationPerformed() {
        return optimizationPerformed;
    }

    public void notifyOptimizationPerformed() {
        this.optimizationPerformed = true;
    }

    @Override
    public R visitNullValueNode(NullValueNode nullValueNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitIntegerValueNode(IntegerNode integerNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitBooleanValueNode(BooleanNode booleanNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitThisNode(ThisNode thisNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitConstantNode(ConstantNode constantNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadLocalNode(LoadLocalNode loadLocalNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadGlobalNode(LoadGlobalNode loadGlobalNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadMemberNode(LoadMemberNode loadMemberNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadMemberFastNode(LoadMemberFastNode loadMemberFastNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadStaticNode(LoadStaticNode loadStaticNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadAbstractImplNode(LoadAbstractImplNode loadAbstractImplNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitLoadNameNode(LoadNameNode loadNameNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitContainerReadNode(ContainerReadNode containerReadNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitArgumentNode(ArgumentNode argumentNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitBinaryOperationNode(BinaryOperationNode binaryOperationNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitUnaryOperationNode(UnaryOperationNode unaryOperationNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitGetTypeNode(GetTypeNode getTypeNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitRangeNode(RangeNode rangeNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitArrayNode(ArrayNode arrayNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitDictNode(DictNode dictNode, P p) {
        return defaultAction(p);
    }

    @Override
    public R visitCallNode(CallNode callNode, P p) {
        return defaultAction(p);
    }
}
