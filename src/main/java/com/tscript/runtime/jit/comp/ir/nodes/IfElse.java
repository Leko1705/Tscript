package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRControl;
import com.tscript.runtime.jit.comp.ir.IRControlVisitor;
import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.ArrayList;
import java.util.List;

public class IfElse extends IRControl {

    private final IRControl control;

    private IRValue condition;

    private final IRControl ifBranch;
    private final IRControl elseBranch;

    public IfElse(IRControl control, IRValue condition, IRControl ifBranch, IRControl elseBranch, Graph graph) {
        super(graph);
        this.control = control;
        control.getSuccessors().add(this);

        this.condition = condition;
        this.ifBranch = ifBranch;
        this.elseBranch = elseBranch;

        condition.getUsers().add(this);
    }

    public IRValue getCondition() {
        return condition;
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
        return new ArrayList<>(List.of(ifBranch, elseBranch));
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (oldInput == condition) {
            condition = newInput;
        }
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of(getCondition());
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitIfElse(this, p);
    }

}
