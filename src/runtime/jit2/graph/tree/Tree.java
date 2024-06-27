package runtime.jit2.graph.tree;

public interface Tree {

    <R, P> R accept(TreeVisitor<R, P> visitor, P p);

    default <R, P> R accept(TreeVisitor<R, P> visitor) {
        return accept(visitor, null);
    }

    default void replace(Tree toReplace, Tree tree) { }

    default void remove(Tree tree){ }
}
