package runtime.jit.graph;

public interface ExpressionNode {

    <P, R> R accept(ExpressionVisitor<P, R> walker, P param);

    default <P, R> R accept(ExpressionVisitor<P, R> walker) {
        return accept(walker, null);
    }

}
