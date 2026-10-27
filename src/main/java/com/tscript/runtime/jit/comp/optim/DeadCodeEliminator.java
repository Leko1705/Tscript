package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

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
            changed |= progress;
        }

        if (changed) graph.rehash();
        return changed;
    }

    private boolean isRemovable(IRValue value) {
        return value instanceof Pi
                || value instanceof Global
                || value instanceof Array
                || value instanceof Dict;
    }
}
