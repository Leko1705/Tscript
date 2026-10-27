package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IREffect;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.nodes.Bool;
import com.tscript.runtime.jit.comp.ir.nodes.GuardBool;
import com.tscript.runtime.jit.comp.ir.stamps.BoolStamp;

public class GuardEliminator implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean optimized = false;

        for (IREffect e = graph.startEffect(); e != null; e = e.next) {
            if (e instanceof GuardBool g) {
                IRValue value = g.getValue();

                boolean isTrueConst = (value instanceof Bool b && b.getValue() == true);
                boolean alwaysTrueStamp = (value.stamp instanceof BoolStamp b && b.canBeTrue && !b.canBeFalse);

                if (isTrueConst || alwaysTrueStamp) {
                    g.remove();
                    optimized = true;
                }
            }
        }

        return optimized;
    }

}
