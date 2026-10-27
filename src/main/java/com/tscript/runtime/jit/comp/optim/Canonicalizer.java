package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

import java.util.*;

/**
 * Applies {@link IRValue#canonicalize(CanonicalizerTool)} to every node of the graph until a
 * fixpoint is reached, removes redundant phis and performs global value numbering on the
 * rewritten graph.
 * <p>
 * During graph construction nodes are canonicalized once on creation. Running the canonicalizer
 * again after the graph is complete is necessary because more facts are known then
 * (most importantly the types of loop phis).
 */
public class Canonicalizer implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean changed = false;
        boolean progress = true;
        int rounds = 0;

        while (progress && rounds++ < 32) {
            progress = false;
            graph.typeMap().flush();

            progress |= simplifyPhis(graph);

            CanonicalizerTool tool = graph.canonicalizerTool();
            for (IRValue value : graph.values()) {
                if (value.isDead() || value instanceof Phi) continue;
                IRValue simplified = value.canonicalize(tool);
                if (simplified != value) {
                    replace(value, simplified);
                    graph.typeMap().flush();
                    progress = true;
                }
            }

            progress |= valueNumbering(graph);
            changed |= progress;
        }

        graph.typeMap().flush();
        graph.rehash();
        return changed;
    }

    static void replace(IRValue value, IRValue replacement) {
        // the anchors of the value have to be removed first, otherwise they would anchor the replacement
        if (value.isFixed()) {
            for (IREffect effect : anchorsOf(value)) {
                effect.remove();
            }
        }
        value.replaceAllUsesWith(replacement);
        value.kill();
    }

    static List<IREffect> anchorsOf(IRValue value) {
        List<IREffect> anchors = new ArrayList<>();
        for (IRNode user : value.getUsers()) {
            if (user instanceof Anchor a) anchors.add(a);
        }
        return anchors;
    }

    private boolean simplifyPhis(Graph graph) {
        boolean changed = false;
        boolean progress = true;
        while (progress) {
            progress = false;
            for (LoopHeadNode loop : graph.loops()) {
                for (Phi phi : new ArrayList<>(loop.getPhis())) {
                    IRValue unique = phi.redundantInput();
                    if (unique != null) {
                        graph.replacePhi(phi, unique);
                        progress = changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private boolean valueNumbering(Graph graph) {
        boolean changed = false;
        Map<Key, IRValue> table = new HashMap<>();
        for (IRValue value : graph.values()) {
            if (value.isDead() || !value.isGVNable()) continue;
            Key key = new Key(value);
            IRValue existing = table.putIfAbsent(key, value);
            if (existing != null && existing != value) {
                replace(value, existing);
                changed = true;
            }
        }
        return changed;
    }

    private record Key(IRValue value) {
        @Override
        public int hashCode() {
            return value.valueHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Key k && value.valueEquals(k.value);
        }
    }
}
