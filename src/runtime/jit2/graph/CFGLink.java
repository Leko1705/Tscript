package runtime.jit2.graph;

public interface CFGLink {

    <R, P> R accept(CFGVisitor<R, P> visitor, P p);

    default <R, P> R accept(CFGVisitor<R, P> visitor){
        return accept(visitor, null);
    }

}
