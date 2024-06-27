package runtime.jit2.graph;

public class SimpleLink implements CFGLink {

    public boolean isLoop;
    public CFGNode next;

    public SimpleLink(){
    }

    public SimpleLink(CFGNode next, boolean isLoop) {
        this.next = next;
        this.isLoop = isLoop;
    }

    @Override
    public <R, P> R accept(CFGVisitor<R, P> visitor, P p) {
        return visitor.visitSimpleLink(this, p);
    }
}
