package runtime.jit.graph;

import tscriptc.generation.Opcode;

import java.util.List;

public final class Flow {

    private Flow(){
    }

    private static abstract class AbstractFlowNode implements FlowNode {
        public FlowNode next;
        @Override
        public void setNext(FlowNode next) {
            this.next = next;
        }
        @Override
        public FlowNode getNext() {
            return next;
        }
    }

    public static class StartNode extends AbstractFlowNode {
        @Override
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitStartNode(this, param);
        }
    }

    public static class BeginNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitBeginNode(this, param);
        }
    }

    public static class EndNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitEndNode(this, param);
        }
    }

    public abstract static class AddressNode extends AbstractFlowNode {
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
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitLoadParameterNode(this, param);
        }
    }

    public static class MergeNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitMergeNode(this, param);
        }
    }

    public static class IfNode implements FlowNode {
        public FlowNode T;
        public FlowNode F;
        public ExpressionNode condition;
        public Opcode branchOpcode;
        public IfNode(ExpressionNode condition, Opcode branchOpcode){
            this.condition = condition;
            this.branchOpcode = branchOpcode;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitIfNode(this, param);
        }
        @Override
        public FlowNode getNext() {
            throw new UnsupportedOperationException();
        }
        @Override
        public void setNext(FlowNode next) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ExpressionStatementNode extends AbstractFlowNode {
        public ExpressionNode expression;
        public ExpressionStatementNode(ExpressionNode expr) {
            this.expression = expr;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitExpressionStatementNode(this, param);
        }
    }

    public static class LoopBeginNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitLoopBeginNode(this, param);
        }
    }

    public static class LoopEndNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitLoopEndNode(this, param);
        }
    }

    public static class LoopExitNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitLoopExitNode(this, param);
        }
    }

    public static class ThrowNode extends AbstractFlowNode {
        public ExpressionNode expression;
        public ThrowNode(ExpressionNode expr){
            this.expression = expr;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitThrowNode(this, param);
        }
    }

    public static class TryStartNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitTryStartNode(this, param);
        }
    }

    public static class TryEndNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitTryEndNode(this, param);
        }
    }

    public static class CatchStartNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitCatchStartNode(this, param);
        }
    }

    public static class CatchEndNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitCatchEndNode(this, param);
        }
    }

    public static class ReturnNode extends AbstractFlowNode {
        public ExpressionNode expression;
        public ReturnNode(ExpressionNode expression){
            this.expression = expression;
        }
        @Override
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitReturnNode(this, param);
        }
    }

    public static class NewLineNode extends AbstractFlowNode {
        public int line;
        public NewLineNode(int line) {
            this.line = line;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitNewLineNode(this, param);
        }
    }

    public static class StoreGlobalNode extends AddressNode {
        private ExpressionNode expression;
        public StoreGlobalNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return null;
        }
    }

    public static class StoreLocalNode extends AddressNode {
        public ExpressionNode expression;
        public StoreLocalNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
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
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitStoreStaticNode(this, param);
        }
    }

    public static class StoreMemberNode extends AddressNode {
        public ExpressionNode expression;
        public StoreMemberNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitStoreMemberNode(this, param);
        }
    }

    public static class StoreMemberFastNode extends AddressNode {
        public ExpressionNode expression;
        public StoreMemberFastNode(int address, ExpressionNode expr) {
            super(address);
            this.expression = expr;
        }
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitStoreMemberFastNode(this, param);
        }
    }

    public static class ContainerWriteNode extends AbstractFlowNode {
        public ExpressionNode container;
        public ExpressionNode key;
        public ExpressionNode value;
        public ContainerWriteNode(ExpressionNode container, ExpressionNode key, ExpressionNode value) {
            this.container = container;
            this.key = key;
            this.value = value;
        }
        @Override
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitContainerWriteNode(this, param);
        }
    }

    public static class BreakPointNode extends AbstractFlowNode {
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitBreakPointNode(this, param);
        }
    }

    public static class UseNode extends AbstractFlowNode {
        @Override
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitUseNode(this, param);
        }
    }

    public static class CallSuperNode extends AbstractFlowNode {
        public final List<ExpressionNode> arguments;
        public CallSuperNode(List<ExpressionNode> arguments) {
            this.arguments = arguments;
        }
        @Override
        public <R, P> R accept(FlowWalker<R, P> walker, P param) {
            return walker.visitCallSuperNode(this, param);
        }
    }

}
