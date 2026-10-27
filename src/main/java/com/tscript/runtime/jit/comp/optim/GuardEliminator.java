package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

import java.util.*;

/**
 * Removes guards that can never fail:
 * <ul>
 *     <li>guards whose condition is the constant true</li>
 *     <li>guards whose condition was already checked by an earlier guard. Since the effect chain
 *     is linear, an earlier guard dominates all later ones.</li>
 *     <li>guards comparing an integer with a constant whose outcome follows from the value ranges
 *     established by earlier guards (e.g. {@code x < 10} after {@code x < 5})</li>
 * </ul>
 */
public class GuardEliminator implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean optimized = false;
        Set<IRValue> checked = new HashSet<>();
        Map<IRValue, long[]> ranges = new HashMap<>();
        CanonicalizerTool tool = graph.canonicalizerTool();

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
                else {
                    Bound bound = bound(condition, tool);
                    if (bound == null) continue;
                    long[] range = ranges.computeIfAbsent(bound.value, v -> new long[]{Long.MIN_VALUE, Long.MAX_VALUE});
                    if (range[0] >= bound.lo && range[1] <= bound.hi) {
                        g.remove();
                        optimized = true;
                    }
                    else {
                        range[0] = Math.max(range[0], bound.lo);
                        range[1] = Math.min(range[1], bound.hi);
                    }
                }
            }
        }

        return optimized;
    }

    /**
     * the condition holds iff lo <= value <= hi
     */
    private record Bound(IRValue value, long lo, long hi) { }

    private static Bound bound(IRValue condition, CanonicalizerTool tool) {
        boolean negated = false;
        if (condition instanceof Not not && !tool.isInt(not.getValue())) {
            negated = true;
            condition = not.getValue();
        }
        if (!(condition instanceof BinaryOp op)) return null;
        IRValue lhs = op.getLeft(), rhs = op.getRight();
        boolean constantRight = rhs instanceof Int && tool.isInt(lhs);
        boolean constantLeft = lhs instanceof Int && tool.isInt(rhs);
        if (!constantRight && !constantLeft) return null;
        IRValue value = constantRight ? lhs : rhs;
        long c = ((Int) (constantRight ? rhs : lhs)).getValue();
        long min = Long.MIN_VALUE, max = Long.MAX_VALUE;

        // the range of value for which the (non negated) condition holds
        long lo, hi;
        if (condition instanceof Lt) {
            if (constantRight) { lo = min; hi = c - 1; } else { lo = c + 1; hi = max; }
        }
        else if (condition instanceof Leq) {
            if (constantRight) { lo = min; hi = c; } else { lo = c; hi = max; }
        }
        else if (condition instanceof Equals && !negated) {
            return new Bound(value, c, c);
        }
        else {
            return null;
        }
        if (!negated) return new Bound(value, lo, hi);
        // the complement of a half open range
        return lo == min ? new Bound(value, hi + 1, max) : new Bound(value, min, lo - 1);
    }

}
