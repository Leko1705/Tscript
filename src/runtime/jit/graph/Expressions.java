package runtime.jit.graph;

import tscriptc.generation.Opcode;

import java.util.List;

public final class Expressions {

    private Expressions() {
    }

    public interface ValueNode<T> extends ExpressionNode {
        <P, R> R accept(ExpressionVisitor<P, R> walker, P p);
        T get();
    }

    public static class NullValueNode implements ValueNode<Void> {
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitNullValueNode(this, p);
        }
        @Override
        public Void get() {
            return null;
        }
    }

    public static class IntegerNode implements ValueNode<Integer> {
        private final int value;
        public IntegerNode(int value) {
            this.value = value;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitIntegerValueNode(this, p);
        }
        @Override
        public Integer get() {
            return value;
        }
    }

    public static class BooleanNode implements ValueNode<Boolean> {
        private final boolean value;
        public BooleanNode(boolean value) {
            this.value = value;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitBooleanValueNode(this, p);
        }
        @Override
        public Boolean get() {
            return value;
        }
    }

    public static class ThisNode implements ExpressionNode {
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitThisNode(this, p);
        }
    }

    private static abstract class AddressedNode implements ExpressionNode {
        public final int address;
        private AddressedNode(int address) {
            this.address = address;
        }
    }

    public static class ConstantNode extends AddressedNode {
        public ConstantNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitConstantNode(this, p);
        }
    }

    public static class LoadLocalNode extends AddressedNode {
        public LoadLocalNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitLoadLocalNode(this, p);
        }
    }

    public static class LoadGlobalNode extends AddressedNode {
        public LoadGlobalNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitLoadGlobalNode(this, p);
        }
    }

    public static class LoadMemberNode extends AddressedNode {
        public LoadMemberNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitLoadMemberNode(this, p);
        }
    }

    public static class LoadMemberFastNode extends AddressedNode {
        public LoadMemberFastNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitLoadMemberFastNode(this, p);
        }
    }

    public static class LoadStaticNode extends AddressedNode {
        public LoadStaticNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitLoadStaticNode(this, p);
        }
    }

    public static class LoadAbstractImplNode extends AddressedNode {
        public LoadAbstractImplNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitLoadAbstractImplNode(this, p);
        }
    }

    public static class LoadNameNode extends AddressedNode {
        public LoadNameNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitLoadNameNode(this, p);
        }
    }

    public static class ContainerReadNode implements ExpressionNode {
        public ExpressionNode container;
        public ExpressionNode key;
        public ContainerReadNode(ExpressionNode container, ExpressionNode key) {
            this.container = container;
            this.key = key;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitContainerReadNode(this, p);
        }
    }

    public static class ArgumentNode extends AddressedNode {
        public ExpressionNode value;
        public ArgumentNode(int address, ExpressionNode value) {
            super(address);
            this.value = value;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitArgumentNode(this, p);
        }
    }

    public static class BinaryOperationNode implements ExpressionNode {
        public Opcode operation;
        public ExpressionNode left;
        public ExpressionNode right;
        public BinaryOperationNode(Opcode operation, ExpressionNode left, ExpressionNode right) {
            this.operation = operation;
            this.left = left;
            this.right = right;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitBinaryOperationNode(this, p);

        }
    }

    public static class UnaryOperationNode implements ExpressionNode {
        public Opcode operation;
        public ExpressionNode expression;
        public UnaryOperationNode(Opcode operation, ExpressionNode expression){
            this.operation = operation;
            this.expression = expression;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitUnaryOperationNode(this, p);
        }
    }

    public static class GetTypeNode implements ExpressionNode {
        public ExpressionNode expression;
        public GetTypeNode(ExpressionNode expression) {
            this.expression = expression;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitGetTypeNode(this, p);
        }
    }

    public static class RangeNode implements ExpressionNode {
        public ExpressionNode from;
        public ExpressionNode to;
        public RangeNode(ExpressionNode from, ExpressionNode to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitRangeNode(this, p);
        }
    }

    public static class ArrayNode implements ExpressionNode {
        public List<ExpressionNode> content;
        public ArrayNode(List<ExpressionNode> content) {
            this.content = content;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitArrayNode(this, p);
        }
    }

    public static class DictNode implements ExpressionNode {
        public List<ExpressionNode> keys;
        public List<ExpressionNode> values;
        public DictNode(List<ExpressionNode> keys, List<ExpressionNode> values) {
            this.keys = keys;
            this.values = values;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitDictNode(this, p);
        }
    }

    public static class CallNode implements ExpressionNode {
        public ExpressionNode called;
        public List<ExpressionNode> arguments;
        public CallNode(ExpressionNode called, List<ExpressionNode> arguments) {
            this.called = called;
            this.arguments = arguments;
        }
        @Override
        public <P, R> R accept(ExpressionVisitor<P, R> walker, P p) {
            return walker.visitCallNode(this, p);
        }
    }

}
