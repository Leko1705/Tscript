package runtime.jit2.graph;

public interface CFGVisitor<R, P> {

    R visitSimpleLink(SimpleLink link, P p);

    R visitBranchLink(BranchedLink link, P p);

}
