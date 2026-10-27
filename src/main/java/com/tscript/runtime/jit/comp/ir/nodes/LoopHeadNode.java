package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A loop header. Its predecessors are the control flow entering the loop and (once the loop
 * is closed) the {@link LoopEndNode}. A loop header that never gets closed is degenerate: it is
 * executed exactly once and its phis are equivalent to their entry values.
 */
public class LoopHeadNode extends IRControl {

    private final IRControl entry;
    private LoopEndNode loopEnd;
    private final List<IRControl> successors = new ArrayList<>();
    private final List<Phi> phis = new ArrayList<>();

    /**
     * the bytecode address of the loop header
     */
    public final int ip;

    public LoopHeadNode(Graph graph, IRControl entry, int ip) {
        super(graph);
        this.entry = entry;
        this.ip = ip;
        entry.getSuccessors().add(this);
    }

    public IRControl getEntry() {
        return entry;
    }

    public LoopEndNode getLoopEnd() {
        return loopEnd;
    }

    public boolean isClosed() {
        return loopEnd != null;
    }

    void setLoopEnd(LoopEndNode loopEnd) {
        this.loopEnd = loopEnd;
    }

    public List<Phi> getPhis() {
        return phis;
    }

    @Override
    public List<IRControl> getPredecessors() {
        return loopEnd == null ? List.of(entry) : List.of(entry, loopEnd);
    }

    @Override
    public List<IRControl> getSuccessors() {
        return successors;
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitLoopHead(this, p);
    }
}
