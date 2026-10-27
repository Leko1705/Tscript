package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.*;

import java.util.*;

/**
 * Keeps global variables that are accessed in a loop in registers (loop phis) instead of loading
 * and storing them in every iteration. Globals are written back to memory whenever compiled
 * code is left (every guard and exit of the loop carries write backs).
 * <p>
 * Only applies to loops that cannot observe globals in another way: the loop must not contain
 * calls (which could access the globals) or operations that may report runtime errors (which
 * would leave compiled code without writing the globals back).
 * <p>
 * If the loop narrows the type of a global, the type is speculated on loop entry as well, so
 * that the global can be kept unboxed.
 */
public class GlobalPromotion implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean changed = false;
        for (IREffect effect : graph.effects()) {
            if (effect instanceof LoopBegin begin && begin.getLoop().isClosed() && begin.getLoop().getEntryState() != null) {
                changed |= promote(graph, begin);
            }
        }
        if (changed) {
            graph.typeMap().flush();
            graph.rehash();
        }
        return changed;
    }

    private boolean promote(Graph graph, LoopBegin begin) {
        LoopHeadNode loop = begin.getLoop();

        Set<GlobalRef> globals = new LinkedHashSet<>();
        Map<GlobalRef, IRType> narrowedTypes = new HashMap<>();
        for (IREffect e = begin.next; e != null; e = e.next) {
            if (e instanceof StoreGlobal store) {
                globals.add(store.global);
            }
            else if (e instanceof Anchor anchor) {
                IRValue value = anchor.getValue();
                if (value instanceof Global load) {
                    globals.add(load.global);
                }
                else if (value instanceof Pi pi && pi.getValue() instanceof Global load) {
                    narrowedTypes.putIfAbsent(load.global, pi.type);
                }
                else if (isBarrier(graph, value)) {
                    return false;
                }
            }
            else if (e instanceof ContainerWrite write && !isBuiltinContainer(graph, write.getContainer())) {
                return false;
            }
        }
        if (globals.isEmpty()) return false;

        IREffect preheaderEnd = begin.previous;
        IRControl preheader = preheaderEnd.control;
        int site = siteOf(begin);

        for (GlobalRef global : globals) {
            // load the global on loop entry (and speculate on its type)
            Global initial = new Global(global);
            IREffect last = insertAfter(preheaderEnd, new Anchor(initial, preheader, site));
            preheaderEnd = last;
            IRValue entry = initial;
            IRType type = narrowedTypes.get(global);
            if (type != null) {
                GuardBool guard = new GuardBool(new IsType(initial, type), loop.getEntryState(), loop.getEntryValues(), preheader);
                guard.setSideTraceable(false);
                preheaderEnd = insertAfter(preheaderEnd, guard);
                Pi pi = new Pi(initial, type);
                preheaderEnd = insertAfter(preheaderEnd, new Anchor(pi, preheader, site));
                entry = pi;
            }

            Phi phi = new Phi(loop, entry);
            phi.setGlobal(global);

            IRValue current = phi;
            IREffect e = begin.next;
            while (e != null) {
                IREffect next = e.next;
                if (e instanceof Anchor anchor && anchor.getValue() instanceof Global load && load.global.equals(global)) {
                    load.replaceAllUsesWith(current);
                    anchor.remove();
                    load.kill();
                }
                else if (e instanceof StoreGlobal store && store.global.equals(global)) {
                    current = store.getValue();
                    store.remove();
                }
                else if (e instanceof Guard guard) {
                    guard.addGlobalWrite(global, current);
                }
                e = next;
            }
            phi.setBackedge(current);
        }
        return true;
    }

    private static IREffect insertAfter(IREffect position, IREffect effect) {
        position.insertAfter(effect);
        return effect;
    }

    private static int siteOf(LoopBegin begin) {
        for (IREffect e = begin.next; e != null; e = e.next) {
            if (e instanceof Anchor anchor) return anchor.site;
        }
        return -1;
    }

    /**
     * @return true if evaluating the value may call user code or report a runtime error
     */
    /**
     * @return true if evaluating the value may run arbitrary code (which might access globals)
     */
    private static boolean isBarrier(Graph graph, IRValue value) {
        return value instanceof Call
                || value instanceof ContainerRead read && !isBuiltinContainer(graph, read.getContainer())
                || value instanceof GetIterator;
    }

    private static boolean isBuiltinContainer(Graph graph, IRValue container) {
        IRType type = graph.typeMap().getType(container);
        return type instanceof ArrayType || type instanceof DictType;
    }
}
