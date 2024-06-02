package runtime.jit.graph;

import java.util.List;

public final class Flow {

    private Flow(){
    }

    public static class GotoNode implements ControlFlowNode, LinkedFlowNode {
        private final ControlFlowContext context;
        private FlowNode next;
        public GotoNode(ControlFlowContext context) { this.context = context; }
        @Override public ControlFlowContext getContext() { return context; }
        @Override public FlowNode getNext() { return next; }
        @Override public void setNext(FlowNode next) { this.next = next; }
        @Override public <P, R> R accept(FlowWalker<P, R> walker, P p) {
            return walker.visitGotoNode(this, p);
        }
    }

    public static class BranchNode implements FlowNode {
        public BranchNode(ExpressionNode condition){ this.condition = condition; }
        public ExpressionNode condition;
        public FlowNode T, F;
        @Override public <P, R> R accept(FlowWalker<P, R> walker, P p) {
            return walker.visitBranchNode(this, p);
        }
    }

    public static class PathEndNode implements ControlFlowNode, LinkedFlowNode {
        private final ControlFlowContext context;
        private FlowNode next;
        public PathEndNode(ControlFlowContext context) { this.context = context; }
        @Override public ControlFlowContext getContext() { return context; }
        @Override public FlowNode getNext() { return next; }
        @Override public void setNext(FlowNode next) { this.next = next; }
        @Override public <P, R> R accept(FlowWalker<P, R> walker, P p) {
            return walker.visitPathEndNode(this, p);
        }
    }


    public abstract static class AbstractLinkedNode implements LinkedFlowNode {
        private FlowNode next;
        @Override  public FlowNode getNext() { return next; }
        @Override public void setNext(FlowNode next) { this.next = next; }
    }


    public static class BeginNode extends AbstractLinkedNode {
        @Override public <P, R> R accept(FlowWalker<P, R> walker, P p) {
            return walker.visitBeginNode(this, p);
        }
    }


    public static class StartNode extends AbstractLinkedNode {
        @Override public <P, R> R accept(FlowWalker<P, R> walker, P p) {
            return walker.visitStartNode(this, p);
        }
    }


    public abstract static class AddressNode extends AbstractLinkedNode {
        public final int address;
        public AddressNode(int address) {
            this.address = address;
        }
    }

    public static class LoadParameterNode extends AddressNode {
        public LoadParameterNode(int address) {
            super(address);
        }
        @Override
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitLoadParameterNode(this, param);
        }
    }


    public static class ExpressionStatementNode extends AbstractLinkedNode {
        public ExpressionNode expression;
        public ExpressionStatementNode(ExpressionNode expr) {
            this.expression = expr;
        }
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitExpressionStatementNode(this, param);
        }
    }


    public static class ThrowNode extends AbstractLinkedNode {
        public ExpressionNode expression;
        public ThrowNode(ExpressionNode expr){
            this.expression = expr;
        }
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitThrowNode(this, param);
        }
    }

    public static class TryStartNode extends AbstractLinkedNode {
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitTryStartNode(this, param);
        }
    }

    public static class TryEndNode extends AbstractLinkedNode {
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitTryEndNode(this, param);
        }
    }

    public static class CatchStartNode extends AbstractLinkedNode {
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitCatchStartNode(this, param);
        }
    }

    public static class CatchEndNode extends AbstractLinkedNode {
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitCatchEndNode(this, param);
        }
    }

    public static class ReturnNode extends AbstractLinkedNode {
        public ExpressionNode expression;
        public ReturnNode(ExpressionNode expression){
            this.expression = expression;
        }
        @Override
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitReturnNode(this, param);
        }
    }

    public static class NewLineNode extends AbstractLinkedNode {
        public int line;
        public NewLineNode(int line) {
            this.line = line;
        }
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitNewLineNode(this, param);
        }
    }

    public static class StoreGlobalNode extends AddressNode {
        public ExpressionNode expression;
        public StoreGlobalNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitStoreGlobalNode(this, param);
        }
    }

    public static class StoreLocalNode extends AddressNode {
        public ExpressionNode expression;
        public StoreLocalNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitStoreLocalNode(this, param);
        }
    }

    public static class StoreStaticNode extends AddressNode {
        public ExpressionNode expression;
        public StoreStaticNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        @Override
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitStoreStaticNode(this, param);
        }
    }

    public static class StoreMemberNode extends AddressNode {
        public ExpressionNode expression;
        public StoreMemberNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitStoreMemberNode(this, param);
        }
    }

    public static class StoreMemberFastNode extends AddressNode {
        public ExpressionNode expression;
        public StoreMemberFastNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitStoreMemberFastNode(this, param);
        }
    }

    public static class ContainerWriteNode extends AbstractLinkedNode {
        public ExpressionNode container;
        public ExpressionNode key;
        public ExpressionNode value;
        public ContainerWriteNode(ExpressionNode container, ExpressionNode key, ExpressionNode value) {
            this.container = container;
            this.key = key;
            this.value = value;
        }
        @Override
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitContainerWriteNode(this, param);
        }
    }

    public static class BreakPointNode extends AbstractLinkedNode {
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitBreakPointNode(this, param);
        }
    }

    public static class UseNode extends AbstractLinkedNode {
        @Override
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitUseNode(this, param);
        }
    }

    public static class CallSuperNode extends AbstractLinkedNode {
        public final List<ExpressionNode> arguments;
        public CallSuperNode(List<ExpressionNode> arguments) {
            this.arguments = arguments;
        }
        @Override
        public <P, R> R accept(FlowWalker<P, R> walker, P param) {
            return walker.visitCallSuperNode(this, param);
        }
    }

}
