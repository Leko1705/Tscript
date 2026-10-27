package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

import java.util.*;

/**
 * Replaces reads of constant indices of arrays allocated in the trace by the stored elements,
 * if the array does not escape (it is never written, passed to a call or stored somewhere).
 * Frame states may still refer to the array: deoptimization sees the allocated object.
 * If all reads are replaced, the {@link DeadCodeEliminator} removes the allocation.
 */
public class ScalarReplacement implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean changed = false;
        for (IRValue value : graph.values()) {
            if (!(value instanceof Array array) || array.isDead() || escapes(array)) continue;
            List<IRValue> elements = array.getValues();
            for (IRNode user : new ArrayList<>(array.getUsers())) {
                if (user instanceof ContainerRead read && read.getContainer() == array
                        && read.getKey() instanceof Int key) {
                    int index = key.getValue();
                    if (index >= 0 && index < elements.size()) {
                        Canonicalizer.replace(read, elements.get(index));
                        changed = true;
                    }
                }
            }
        }
        if (changed) graph.rehash();
        return changed;
    }

    private static boolean escapes(IRValue object) {
        for (IRNode user : object.getUsers()) {
            if (user instanceof ContainerRead read) {
                if (read.getKey() == object) return true;
            }
            else if (user instanceof Pi pi) {
                if (escapes(pi)) return true;
            }
            // a phi carries the object into the next iteration, where it is a different
            // object than the one allocated in that iteration
            else if (!(user instanceof Anchor || user instanceof IsType || user instanceof Phi
                    || user instanceof StateCarrier || user instanceof LoopHeadNode)) {
                return true;
            }
        }
        return false;
    }
}
