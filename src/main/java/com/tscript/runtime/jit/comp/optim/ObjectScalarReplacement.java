package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.TObject;

import java.util.*;

/**
 * Removes allocations of class instances that do not escape the trace (escape analysis and
 * scalar replacement).
 * <p>
 * An object does not escape if it is only used to read and write its members (with known
 * indices), to create its methods (function objects owned by the object, which must not escape
 * either: calls of them have to be inlined) and in frame states. Since the effects of a trace form
 * a linear chain, the values of the members are known at every point: reads are replaced by the
 * last value written. Values that turn out to be the object or one of its methods (reads of
 * members holding them, the owner of an inlined method, type narrowings) are aliases and are
 * replaced as well. Guards (and exits) whose state refers to the object record the values of its
 * members ({@link VirtualObjectState}), so that the object can be created when compiled code is
 * left there.
 */
public class ObjectScalarReplacement implements IROptimizer {

    @Override
    public boolean optimize(Graph graph) {
        boolean changed = false;
        for (IREffect effect : graph.effects()) {
            if (effect instanceof Anchor anchor && anchor.getValue() instanceof NewObject object && !object.isDead()) {
                changed |= new Analysis(graph, anchor, object).run();
            }
        }
        if (changed) {
            graph.typeMap().flush();
            graph.rehash();
        }
        return changed;
    }

    private static final class Analysis {

        final Graph graph;
        final Anchor allocation;
        final NewObject object;

        /** the function objects owned by the object */
        final Set<IRValue> methods = new HashSet<>();
        /** nodes that are replaced (reads of members, aliases of the object) */
        final Map<IRValue, IRValue> replacements = new LinkedHashMap<>();
        final List<StoreMember> stores = new ArrayList<>();
        final List<Anchor> removedAnchors = new ArrayList<>();
        final Map<StateCarrier, Map<Integer, IRValue>> snapshots = new LinkedHashMap<>();

        Analysis(Graph graph, Anchor allocation, NewObject object) {
            this.graph = graph;
            this.allocation = allocation;
            this.object = object;
            for (IRNode user : object.getUsers()) {
                if (user instanceof NewFunction function && function.getOwner() == object) methods.add(function);
            }
        }

        boolean run() {
            // the escape check may find further aliases (e.g. type narrowings), which the walk
            // has to know about: repeat until nothing new is found
            for (int round = 0; round < 4; round++) {
                stores.clear();
                removedAnchors.clear();
                snapshots.clear();
                if (!walk()) return false;
                int known = replacements.size();
                if (escapes()) return false;
                if (replacements.size() == known) {
                    apply();
                    return true;
                }
            }
            return false;
        }

        IRValue resolve(IRValue value) {
            IRValue replacement;
            while ((replacement = replacements.get(value)) != null) value = replacement;
            return value;
        }

        boolean isObject(IRValue value) {
            value = resolve(value);
            if (value == object) return true;
            if (value instanceof Pi pi && isObject(pi.getValue())) return true;
            return value instanceof FunctionOwner owner && isMethod(owner.getValue());
        }

        boolean isMethod(IRValue value) {
            return methods.contains(resolve(value));
        }

        boolean isAlias(IRValue value) {
            return isObject(value) || isMethod(value);
        }

        /**
         * Walks the effects after the allocation and computes the values of the members.
         * @return false if the object can not be replaced
         */
        boolean walk() {
            removedAnchors.add(allocation);
            Map<Integer, IRValue> members = new TreeMap<>();

            for (IREffect e = allocation.next; e != null; e = e.next) {
                if (e instanceof LoopBegin) {
                    // an object allocated before a loop must not be used in the loop (the members
                    // would differ between iterations)
                    return !usedFrom(e);
                }
                if (e instanceof Anchor anchor) {
                    IRValue value = anchor.getValue();
                    if (methods.contains(value)) {
                        removedAnchors.add(anchor);
                    }
                    else if (value instanceof LoadMember load && isObject(load.getObject())) {
                        if (!isIndexed(load.access)) return false;
                        IRValue member = members.get(load.index);
                        if (member == null) member = initialValue(load.index);
                        if (member == null) return false;
                        replacements.put(load, resolve(member));
                    }
                    else if (isObject(value)) {
                        // the owner of an inlined method or a type narrowing of the object
                        replacements.put(value, object);
                    }
                }
                else if (e instanceof StoreMember store) {
                    if (isObject(store.getObject())) {
                        if (!isIndexed(store.access) || isObject(store.getValue())) return false;
                        members.put(store.index, resolve(store.getValue()));
                        stores.add(store);
                    }
                    else if (isAlias(store.getValue())) {
                        return false;
                    }
                }
                else if (e instanceof StateCarrier carrier && refersTo(carrier)) {
                    snapshots.put(carrier, new TreeMap<>(members));
                }
            }
            if (graph.exitControl() instanceof Exit exit && refersTo(exit)) {
                snapshots.put(exit, new TreeMap<>(members));
            }
            return true;
        }

        /**
         * @return the value of the member before the constructor wrote it, null if it can not be expressed
         */
        IRValue initialValue(int index) {
            TObject template = object.type.getInstanceMemberTemplate(index);
            if (template == null || template == Null.INSTANCE) return graph.builder().nullValue();
            return null;
        }

        boolean refersTo(StateCarrier carrier) {
            for (IRValue value : carrier.getStateValues()) {
                if (isAlias(value)) return true;
            }
            return false;
        }

        boolean usedFrom(IREffect start) {
            for (IREffect e = start; e != null; e = e.next) {
                List<IRValue> inputs = new ArrayList<>(e.getInputs());
                if (e instanceof Anchor anchor) inputs.addAll(anchor.getValue().getInputs());
                for (IRValue input : inputs) if (isAlias(input)) return true;
            }
            return false;
        }

        /**
         * Checks the users of the object, its methods and all their aliases.
         */
        boolean escapes() {
            Set<IRValue> aliases = new LinkedHashSet<>();
            aliases.add(object);
            aliases.addAll(methods);
            for (Map.Entry<IRValue, IRValue> entry : replacements.entrySet()) {
                if (isAlias(entry.getValue())) aliases.add(entry.getKey());
            }
            Deque<IRValue> work = new ArrayDeque<>(aliases);
            while (!work.isEmpty()) {
                IRValue alias = work.poll();
                boolean isMethod = isMethod(alias);
                for (IRNode user : alias.getUsers()) {
                    if (user instanceof Anchor) continue;
                    if (user instanceof LoadMember || user instanceof StoreMember) {
                        if (replacements.containsKey(user) || stores.contains(user)) continue;
                        return true;
                    }
                    if (user instanceof NewFunction function && methods.contains(function)) continue;
                    if (user instanceof IsType || user instanceof IsCallee) continue; // folded once replaced
                    if (user instanceof Pi || user instanceof FunctionOwner) {
                        IRValue value = (IRValue) user;
                        if (user instanceof FunctionOwner && !isMethod) return true;
                        if (aliases.add(value)) {
                            replacements.putIfAbsent(value, object);
                            work.add(value);
                        }
                        continue;
                    }
                    if (user instanceof StateCarrier carrier && onlyInState(carrier, alias)) continue;
                    return true;
                }
            }
            return false;
        }

        void apply() {
            // the states first (so that they are updated when reads are replaced)
            for (Map.Entry<StateCarrier, Map<Integer, IRValue>> entry : snapshots.entrySet()) {
                Map<Integer, IRValue> snapshot = entry.getValue();
                int[] indices = new int[snapshot.size()];
                List<IRValue> values = new ArrayList<>(snapshot.size());
                int i = 0;
                for (Map.Entry<Integer, IRValue> member : snapshot.entrySet()) {
                    indices[i++] = member.getKey();
                    values.add(resolve(member.getValue()));
                }
                entry.getKey().addVirtualObject(object, indices, values);
            }
            for (IRValue replaced : replacements.keySet()) {
                if (!replaced.isDead()) Canonicalizer.replace(replaced, resolve(replaced));
            }
            for (StoreMember store : stores) store.remove();
            for (Anchor anchor : removedAnchors) anchor.remove();
            if (Boolean.getBoolean("tscript.jit.debug")) {
                System.err.println("[jit] removed allocation of " + object.type.getName()
                        + " (created at " + snapshots.size() + " exits)");
            }
        }
    }

    private static boolean isIndexed(MemberAccess access) {
        return access == MemberAccess.FIELD || access == MemberAccess.INTERNAL;
    }

    /**
     * @return true if the value is only part of the frame state of the carrier (and not e.g. the
     * value of a promoted global written back on exit)
     */
    private static boolean onlyInState(StateCarrier carrier, IRValue value) {
        if (carrier.getGlobalWriteValues().contains(value)) return false;
        if (carrier instanceof GuardBool guard && guard.getCondition() == value) return false;
        for (VirtualObjectState state : carrier.getVirtualObjects()) {
            if (carrier.getVirtualObjectValues(state).contains(value)) return false;
        }
        return carrier instanceof Guard || carrier instanceof Exit;
    }
}
