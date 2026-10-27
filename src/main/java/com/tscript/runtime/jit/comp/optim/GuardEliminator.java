package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Removes guards that can never fail:
 * <ul>
 *     <li>guards whose condition is the constant true</li>
 *     <li>guards whose condition was already checked by an earlier guard. Since the effect chain
 *     is linear, an earlier guard dominates all later ones.</li>
 * </ul>
 */
public class GuardEliminator implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean optimized = false;
        Set<IRValue> checked = new HashSet<>();

        for (IREffect e : graph.effects()) {
            if (e instanceof GuardBool g) {
                IRValue condition = g.getCondition();
                if (condition instanceof Bool b && b.value) {
                    g.remove();
                    optimized = true;
                }
                else if (!checked.add(condition)) {
                    g.remove();
                    optimized = true;
                }
            }
        }

        return optimized;
    }

}
