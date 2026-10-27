package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.stamps.*;
import com.tscript.runtime.jit.comp.utils.IRValueScanner;

import java.util.*;
public class ConditionalConstantPropagator implements IROptimizer,
        IRControlVisitor<Void, Void>, IREffectVisitor<Void, Void> {

    private boolean changed;
    private Graph graph;

    @Override
    public boolean optimize(Graph graph) {
        this.graph = graph;
        this.changed = false;
        graph.startControl().accept(this);
        return changed;
    }

    @Override
    public Void visitStart(StartEffect node, Void unused) {
        return null;
    }

    @Override
    public Void visitNewLine(NewLine node, Void unused) {
        return null;
    }

    @Override
    public Void visitGuardBool(GuardBool node, Void unused) {
        IRValue condition = node.getValue();
        Stamp currentStamp = condition.stamp;

        if (currentStamp instanceof BoolStamp bool && bool.isConstant()) {
            if (bool.getConstantValue()) {
                node.remove(); // Guard is always true
                changed = true;
            } else {
                // Guard is always false. This path is dead.
                updateStamp(condition, IllegalStamp.INSTANCE);
            }
            return null;
        }

        // Since we passed this guard, the condition must be true
        updateStamp(condition, BoolStamp.ALWAYS_TRUE);

        // enforce assumed condition actually holds by updating the stamp of the condition
        condition.accept(new ConstraintPropagator());

        return null;
    }

    @Override
    public Void visitGuardFunctionIdentity(GuardFunctionIdentity node, Void unused) {
        IRValue value = node.getValue();

        // If the stamp already knows the identity, and it matches, the guard is dead.
        if (value.stamp instanceof FunctionStamp f && f.identity == node.identity) {
            node.remove();
            changed = true;
            return null;
        }

        // Otherwise, we "learn" the identity.
        // Assuming you have a FunctionStamp that can hold a specific ID.
        updateStamp(value, new FunctionStamp(node.identity));
        return null;
    }

    @Override
    public Void visitValueEffect(ValueEffect node, Void unused) {
        return null;
    }

    @Override
    public Void visitMergeEffect(MergeEffect node, Void unused) {
        return null;
    }

    @Override
    public Void visitPushFrame(PushFrame node, Void unused) {
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, Void unused) {
        return null;
    }

    private void updateStamp(IRValue node, Stamp newInfo) {
        Stamp narrowed = node.stamp.intersection(newInfo);
        if (!narrowed.equals(node.stamp)) {
            node.stamp = narrowed;
            changed = true;
        }
    }

    @Override
    public Void visitStart(StartControl node, Void unused) {
        propagate(node);
        return null;
    }

    @Override
    public Void visitReturn(Return node, Void unused) {
        propagate(node);
        return null;
    }

    @Override
    public Void visitMerge(MergeControl node, Void unused) {
        propagate(node);
        // At a merge, Phis must UNION (Meet) their incoming stamps
        /* TODO for later
        for (Phi phi : node.getPhis()) {
            Stamp merged = IllegalStamp.INSTANCE;
            for (IRValue input : phi.getInputs()) {
                merged = merged.union(input.stamp);
            }
            updateStamp(phi, merged);
        }
         */
        return null;
    }

    @Override
    public Void visitIfElse(IfElse node, Void unused) {
        propagate(node);
        return null;
    }

    private void propagate(IRControl control) {
        for (IREffect effect : graph.getEffectsOf(control)) {
            effect.accept(this);
        }
    }


    private class ConstraintPropagator extends IRValueScanner<Void, Void> {
        @Override
        public Void visitLt(Lt node, Void unused) {
            Stamp left = node.getLeft().stamp;
            Stamp right = node.getRight().stamp;

            if (left instanceof IntegerStamp l && right instanceof IntegerStamp r) {
                // If x < y, then x.max is at most y.max - 1
                updateStamp(node.getLeft(), new IntegerStamp(l.min, r.max - 1));
                // And y.min is at least x.min + 1
                updateStamp(node.getRight(), new IntegerStamp(l.min + 1, r.max));
            }
            if (left instanceof RealStamp l && right instanceof RealStamp r) {
                updateStamp(node.getLeft(), new RealStamp(l.min, Math.nextDown(r.max)));
                updateStamp(node.getRight(), new RealStamp(Math.nextUp(l.min), r.max));
            }

            return null;
        }

        @Override
        public Void visitGt(Gt node, Void unused) {
            throw new IllegalStateException("Should have been canonicalized earlier");
        }

        @Override
        public Void visitEquals(Equals node, Void unused) {
            // Learn that lhs stamp must match rhs stamp
            updateStamp(node.getLeft(), node.getRight().stamp);
            updateStamp(node.getRight(), node.getLeft().stamp);
            return null;
        }
    }

}