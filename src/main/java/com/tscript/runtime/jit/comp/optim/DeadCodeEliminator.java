package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

import java.util.*;

/**
 * Removes fixed nodes whose result is unused and whose evaluation has no observable effect
 * (type narrowing, global loads and allocations), as well as speculative type guards that only
 * existed to narrow a value that turned out to be unused.
 */
public class DeadCodeEliminator implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean changed = false;
        boolean progress = true;

        while (progress) {
            progress = false;
            for (IREffect effect : graph.effects()) {
                if (effect instanceof Anchor anchor) {
                    IRValue value = anchor.getValue();
                    if (!value.isFixed()) {
                        // the anchored node was canonicalized into a floating node
                        anchor.remove();
                        progress = true;
                    }
                    else if (value.getUsers().size() == 1 && isRemovable(value)) {
                        anchor.remove();
                        value.kill();
                        progress = true;
                    }
                }
                else if (effect instanceof GuardBool guard && guard.getNarrowed() != null) {
                    Pi narrowed = guard.getNarrowed();
                    if (narrowed.isDead()) {
                        guard.remove();
                        progress = true;
                    }
                }
            }
            progress |= removeDeadPhis(graph);
            changed |= progress;
        }

        if (changed) graph.rehash();
        return changed;
    }

    /**
     * Removes loop phis that are only used by (dead) phis, e.g. the phis of locals that are dead
     * at the loop header.
     */
    private boolean removeDeadPhis(Graph graph) {
        Set<Phi> live = new HashSet<>();
        Deque<Phi> worklist = new ArrayDeque<>();
        List<Phi> all = new ArrayList<>();
        for (LoopHeadNode loop : graph.loops()) {
            for (Phi phi : loop.getPhis()) {
                all.add(phi);
                for (IRNode user : phi.getUsers()) {
                    if (!(user instanceof Phi)) {
                        live.add(phi);
                        worklist.add(phi);
                        break;
                    }
                }
            }
        }
        while (!worklist.isEmpty()) {
            Phi phi = worklist.poll();
            for (IRValue input : phi.getInputs()) {
                if (input instanceof Phi p && live.add(p)) worklist.add(p);
            }
        }
        boolean changed = false;
        for (Phi phi : all) {
            if (!live.contains(phi)) {
                phi.getLoop().getPhis().remove(phi);
                phi.kill();
                changed = true;
            }
        }
        return changed;
    }

    private boolean isRemovable(IRValue value) {
        return value instanceof Pi
                || value instanceof Global
                || value instanceof Array
                || value instanceof Dict
                || value instanceof NewRangeIterator
                || value instanceof GetSuper
                || value instanceof LoadMember load && (load.access == MemberAccess.FIELD || load.access == MemberAccess.INTERNAL);
    }
}
