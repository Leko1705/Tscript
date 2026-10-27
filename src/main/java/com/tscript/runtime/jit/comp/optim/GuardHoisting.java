package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

import java.util.*;

/**
 * Moves guards whose condition is loop invariant in front of the loop, so that they are checked
 * once instead of in every iteration. A hoisted guard leaves compiled code with the state on loop
 * entry: the interpreter then executes the loop (and finds the same condition) itself.
 * Type narrowings (Pi nodes) established by hoisted guards are moved along with them.
 */
public class GuardHoisting implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean changed = false;
        for (IREffect effect : graph.effects()) {
            if (effect instanceof LoopBegin begin && begin.getLoop().isClosed() && begin.getLoop().getEntryState() != null) {
                changed |= hoist(begin);
            }
        }
        if (changed) graph.rehash();
        return changed;
    }

    private boolean hoist(LoopBegin begin) {
        LoopHeadNode loop = begin.getLoop();

        Set<IRValue> variant = Collections.newSetFromMap(new IdentityHashMap<>());
        variant.addAll(loop.getPhis());
        for (IREffect e = begin.next; e != null; e = e.next) {
            if (e instanceof Anchor anchor) variant.add(anchor.getValue());
        }

        Map<IRValue, Boolean> memo = new IdentityHashMap<>();
        Set<IRValue> hoistedConditions = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean changed = false;

        IREffect e = begin.next;
        while (e != null) {
            IREffect next = e.next;
            if (e instanceof GuardBool guard
                    && !guard.isLoopExit()
                    && guard.getGlobalWrites().isEmpty()
                    && isInvariant(guard.getCondition(), variant, memo)) {
                IRValue condition = guard.getCondition();
                GuardBool hoisted = new GuardBool(condition, loop.getEntryState(), loop.getEntryValues(), begin.previous.control);
                hoisted.setSideTraceable(false);
                begin.previous.insertAfter(hoisted);
                guard.remove();
                hoistedConditions.add(condition);
                changed = true;
            }
            else if (e instanceof Anchor anchor
                    && anchor.getValue() instanceof Pi pi
                    && isInvariant(pi.getValue(), variant, memo)
                    && isNarrowedBy(pi, hoistedConditions)) {
                Anchor moved = new Anchor(pi, begin.previous.control, anchor.site);
                anchor.remove();
                begin.previous.insertAfter(moved);
                variant.remove(pi);
                memo.clear();
                changed = true;
            }
            e = next;
        }
        return changed;
    }

    private static boolean isNarrowedBy(Pi pi, Set<IRValue> conditions) {
        for (IRValue condition : conditions) {
            if (condition instanceof IsType isType && isType.getValue() == pi.getValue() && isType.type.equals(pi.type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInvariant(IRValue value, Set<IRValue> variant, Map<IRValue, Boolean> memo) {
        if (variant.contains(value)) return false;
        Boolean known = memo.get(value);
        if (known != null) return known;
        boolean result = true;
        if (!value.isFixed()) {
            for (IRValue input : value.getInputs()) {
                if (!isInvariant(input, variant, memo)) {
                    result = false;
                    break;
                }
            }
        }
        memo.put(value, result);
        return result;
    }
}
