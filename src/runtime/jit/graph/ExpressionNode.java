package runtime.jit.graph;

public interface ExpressionNode {

    <R, P> R accept(ExpressionVisitor<R, P> walker, P param);

}
