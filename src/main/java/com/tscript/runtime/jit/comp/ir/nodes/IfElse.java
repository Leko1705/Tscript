package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.List;

/**
 * Two-way branch. Not produced by the tracer (traces are linear and use guards instead);
 * intended for a future method JIT front end.
 */
public class IfElse extends IRControl {

    private final IRControl control;
    private final IRControl ifBranch;
    private final IRControl elseBranch;

    public IfElse(IRControl control, IRValue condition, IRControl ifBranch, IRControl elseBranch, Graph graph) {
        super(graph, condition);
        this.control = control;
        control.getSuccessors().add(this);
        this.ifBranch = ifBranch;
        this.elseBranch = elseBranch;
    }

    public IRValue getCondition() {
        return input(0);
    }

    public IRControl getIfBranch() {
        return ifBranch;
    }

    public IRControl getElseBranch() {
        return elseBranch;
    }

    @Override
    public List<IRControl> getPredecessors() {
        return List.of(control);
    }

    @Override
    public List<IRControl> getSuccessors() {
        return List.of(ifBranch, elseBranch);
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitIfElse(this, p);
    }
}
