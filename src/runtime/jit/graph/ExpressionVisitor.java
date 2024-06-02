package runtime.jit.graph;

import static runtime.jit.graph.Expressions.*;

public interface ExpressionVisitor<P, R> {

    R visitNullValueNode(NullValueNode nullValueNode, P p);

    R visitIntegerValueNode(IntegerNode integerNode, P p);

    R visitBooleanValueNode(BooleanNode booleanNode, P p);

    R visitThisNode(ThisNode thisNode, P p);

    R visitConstantNode(ConstantNode constantNode, P p);

    R visitLoadLocalNode(LoadLocalNode loadLocalNode, P p);

    R visitLoadGlobalNode(LoadGlobalNode loadGlobalNode, P p);

    R visitLoadMemberNode(LoadMemberNode loadMemberNode, P p);

    R visitLoadMemberFastNode(LoadMemberFastNode loadMemberFastNode, P p);

    R visitLoadStaticNode(LoadStaticNode loadStaticNode, P p);

    R visitLoadAbstractImplNode(LoadAbstractImplNode loadAbstractImplNode, P p);

    R visitLoadNameNode(LoadNameNode loadNameNode, P p);

    R visitContainerReadNode(ContainerReadNode containerReadNode, P p);

    R visitArgumentNode(ArgumentNode argumentNode, P p);

    R visitBinaryOperationNode(BinaryOperationNode binaryOperationNode, P p);

    R visitUnaryOperationNode(UnaryOperationNode unaryOperationNode, P p);

    R visitGetTypeNode(GetTypeNode getTypeNode, P p);

    R visitRangeNode(RangeNode rangeNode, P p);

    R visitArrayNode(ArrayNode arrayNode, P p);

    R visitDictNode(DictNode dictNode, P p);

    R visitCallNode(CallNode callNode, P p);
}
