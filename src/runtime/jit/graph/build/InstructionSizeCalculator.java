package runtime.jit.graph.build;

import runtime.jit.graph.ExpressionNode;
import runtime.jit.graph.ExpressionVisitor;
import runtime.jit.graph.Expressions;

public class InstructionSizeCalculator implements ExpressionVisitor<Void, Integer> {

    @Override
    public Integer visitNullValueNode(Expressions.NullValueNode nullValueNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitIntegerValueNode(Expressions.IntegerNode integerNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitBooleanValueNode(Expressions.BooleanNode booleanNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitThisNode(Expressions.ThisNode thisNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitConstantNode(Expressions.ConstantNode constantNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitLoadLocalNode(Expressions.LoadLocalNode loadLocalNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitLoadGlobalNode(Expressions.LoadGlobalNode loadGlobalNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitLoadMemberNode(Expressions.LoadMemberNode loadMemberNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitLoadMemberFastNode(Expressions.LoadMemberFastNode loadMemberFastNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitLoadStaticNode(Expressions.LoadStaticNode loadStaticNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitLoadAbstractImplNode(Expressions.LoadAbstractImplNode loadAbstractImplNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitLoadNameNode(Expressions.LoadNameNode loadNameNode, Void unused) {
        return 1;
    }

    @Override
    public Integer visitContainerReadNode(Expressions.ContainerReadNode containerReadNode, Void unused) {
        return 1 + containerReadNode.container.accept(this) + containerReadNode.key.accept(this);
    }

    @Override
    public Integer visitArgumentNode(Expressions.ArgumentNode argumentNode, Void unused) {
        return 1 + argumentNode.value.accept(this);
    }

    @Override
    public Integer visitBinaryOperationNode(Expressions.BinaryOperationNode binaryOperationNode, Void unused) {
        return 1 + binaryOperationNode.left.accept(this) + binaryOperationNode.right.accept(this);
    }

    @Override
    public Integer visitUnaryOperationNode(Expressions.UnaryOperationNode unaryOperationNode, Void unused) {
        return 1 + unaryOperationNode.expression.accept(this);
    }

    @Override
    public Integer visitGetTypeNode(Expressions.GetTypeNode getTypeNode, Void unused) {
        return 1 + getTypeNode.expression.accept(this);
    }

    @Override
    public Integer visitRangeNode(Expressions.RangeNode rangeNode, Void unused) {
        return 1 + rangeNode.from.accept(this) + rangeNode.to.accept(this);
    }

    @Override
    public Integer visitArrayNode(Expressions.ArrayNode arrayNode, Void unused) {
        int cnt = 1;
        for (ExpressionNode exp : arrayNode.content)
            cnt += exp.accept(this);
        return cnt;
    }

    @Override
    public Integer visitDictNode(Expressions.DictNode dictNode, Void unused) {
        int cnt = 1;
        for (int i = 0; i < dictNode.keys.size(); i++)
            cnt += dictNode.keys.get(i).accept(this) + dictNode.values.get(i).accept(this);
        return cnt;
    }

    @Override
    public Integer visitCallNode(Expressions.CallNode callNode, Void unused) {
        int cnt = callNode.called.accept(this);
        for (ExpressionNode exp : callNode.arguments)
            cnt += exp.accept(this);
        return cnt;
    }
}
