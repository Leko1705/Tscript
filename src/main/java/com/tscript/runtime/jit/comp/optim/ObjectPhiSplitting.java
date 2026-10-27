package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.core.VirtualType;
import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.utils.Conversion;

import java.util.*;

/**
 * Removes loop phis of objects that are created anew in every iteration (e.g.
 * {@code v = v.plus(w)}): if the object on loop entry and the object at the end of the body are
 * allocated in the trace, are instances of the same class and do not escape otherwise, the phi
 * is replaced by one phi per member. The object the phi stood for becomes an object that is only
 * created when a guard fails (see {@link ObjectScalarReplacement}), and both allocations can be
 * removed.
 * <p>
 * Only objects that are not changed through the phi are handled (the members of the phi are the
 * values of the iteration start). Methods are not carried in phis: they are created by the
 * constructor (one function object per instance) and are recreated for the object of the phi.
 */
public class ObjectPhiSplitting implements IROptimizer {

    private static final Map<VirtualType, Optional<Map<Integer, Integer>>> methodMembers = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public boolean optimize(Graph graph) {
        boolean changed = false;
        for (IREffect effect : graph.effects()) {
            if (effect instanceof LoopBegin begin && begin.getLoop().isClosed()) {
                for (Phi phi : new ArrayList<>(begin.getLoop().getPhis())) {
                    changed |= split(graph, begin, phi);
                }
            }
        }
        changed |= replaceCarriedReads(graph);
        if (changed) {
            graph.typeMap().flush();
            graph.rehash();
        }
        return changed;
    }

    /**
     * Replaces reads of members of carried objects that appeared after the split (e.g. reads of
     * {@code this} in inlined methods, once the owner of the method was folded).
     */
    private static boolean replaceCarriedReads(Graph graph) {
        boolean changed = false;
        for (IREffect effect : graph.effects()) {
            if (effect instanceof Anchor anchor && anchor.getValue() instanceof LoadMember load
                    && load.getObject() instanceof NewObject object && object.isCarried()
                    && (load.access == MemberAccess.FIELD || load.access == MemberAccess.INTERNAL)) {
                IRValue member = object.carriedMember(load.index);
                if (member == null) continue;
                Canonicalizer.replace(load, member);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Checks that the phi and its aliases (methods read from it, the owners of these methods,
     * type narrowings) are only read and part of frame states.
     */
    private static boolean isReadOnly(Phi phi, Map<Integer, Integer> methods, List<LoadMember> loads, List<StateCarrier> carriers) {
        Set<IRValue> objects = new HashSet<>(List.of(phi));
        Set<IRValue> methodValues = new HashSet<>();
        Deque<IRValue> work = new ArrayDeque<>(objects);
        while (!work.isEmpty()) {
            IRValue alias = work.poll();
            boolean isObject = objects.contains(alias);
            for (IRNode user : alias.getUsers()) {
                if (user instanceof Anchor) continue;
                if (isObject && user instanceof LoadMember load && load.getObject() == alias
                        && (load.access == MemberAccess.FIELD || load.access == MemberAccess.INTERNAL)) {
                    if (alias == phi) loads.add(load);
                    if (methods.containsKey(load.index) && methodValues.add(load)) work.add(load);
                    continue;
                }
                if (isObject && (user instanceof IsType)) continue;
                if (isObject && user instanceof Pi pi) {
                    if (objects.add(pi)) work.add(pi);
                    continue;
                }
                if (!isObject && user instanceof IsCallee) continue;
                if (!isObject && user instanceof FunctionOwner owner) {
                    if (objects.add(owner)) work.add(owner);
                    continue;
                }
                if (user instanceof StateCarrier carrier && onlyInState(carrier, alias)) {
                    // the state needs the object (also if it only refers to one of its aliases)
                    if (!carriers.contains(carrier)) carriers.add(carrier);
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private boolean split(Graph graph, LoopBegin begin, Phi phi) {
        if (!(phi.getEntry() instanceof NewObject entry) || !(phi.getBackedge() instanceof NewObject backedge)) return false;
        if (entry.type != backedge.type || entry == backedge) return false;
        VirtualType type = entry.type;
        if (type.getSuperType() != null) return false;
        Map<Integer, Integer> methods = methodMembers(type);
        if (methods == null) return false;

        // the phi may only be read (also by inlined methods) and be part of frame states
        Map<Integer, Integer> methodIndices = methods;
        List<LoadMember> loads = new ArrayList<>();
        List<StateCarrier> carriers = new ArrayList<>();
        if (!isReadOnly(phi, methodIndices, loads, carriers)) return false;

        // the allocations must be removable once the phi is gone
        if (!isBefore(entry, begin) || isBefore(backedge, begin)) return false;
        if (!ObjectScalarReplacement.isRemovableExcept(graph, entry, phi)) return false;
        if (!ObjectScalarReplacement.isRemovableExcept(graph, backedge, phi)) return false;

        LoopHeadNode loop = begin.getLoop();
        int memberCount = type.getInstanceFields().size();
        Map<Integer, IRValue> members = new TreeMap<>();
        Map<Integer, NewFunction> methodObjects = new HashMap<>();

        IREffect preheaderEnd = begin.previous;
        IREffect bodyEnd = begin;
        while (bodyEnd.next != null) bodyEnd = bodyEnd.next;

        Module module = ((VirtualFunction) type.getDeclaredConstructor()).getMetadata().module();
        int moduleId = ObjectCache.putId(module);
        for (int index = 0; index < memberCount; index++) {
            Integer function = methods.get(index);
            if (function != null) {
                continue; // created below (owned by the object)
            }
            // the member on loop entry and at the end of the body
            LoadMember atEntry = new LoadMember(entry, MemberAccess.FIELD, index, null);
            preheaderEnd = insertAfter(preheaderEnd, new Anchor(atEntry, preheaderEnd.control, siteOf(begin)));
            LoadMember atEnd = new LoadMember(backedge, MemberAccess.FIELD, index, null);
            bodyEnd = insertAfter(bodyEnd, new Anchor(atEnd, bodyEnd.control, siteOf(begin)));
            Phi member = new Phi(loop, atEntry);
            member.setBackedge(atEnd);
            members.put(index, member);
        }

        int[] dataIndices = new int[members.size()];
        List<IRValue> dataValues = new ArrayList<>(members.values());
        int d = 0;
        for (int index : members.keySet()) dataIndices[d++] = index;
        Map<Integer, IRValue> methodValues = new HashMap<>();
        NewObject object = new NewObject(type, ObjectCache.putId(type), dataIndices, dataValues, methodValues);
        for (Map.Entry<Integer, Integer> method : methods.entrySet()) {
            int function = method.getValue();
            int metadataId = ObjectCache.putId(module.getFunctionArea().loadFunction(function).getMetadata());
            NewFunction created = new NewFunction(moduleId, function, metadataId, object);
            methodValues.put(method.getKey(), created);
            members.put(method.getKey(), created);
        }

        int[] keys = new int[members.size()];
        List<IRValue> values = new ArrayList<>(members.size());
        int i = 0;
        for (Map.Entry<Integer, IRValue> member : members.entrySet()) {
            keys[i++] = VirtualObjectState.key(0, member.getKey());
            values.add(member.getValue());
        }
        for (StateCarrier carrier : carriers) carrier.addVirtualObject(object, keys, values);
        for (LoadMember load : loads) Canonicalizer.replace(load, members.get(load.index));
        phi.replaceAllUsesWith(object);
        loop.getPhis().remove(phi);
        phi.kill();

        if (Boolean.getBoolean("tscript.jit.debug")) {
            System.err.println("[jit] split loop phi of " + type.getName() + " into " + (members.size() - methods.size()) + " member phis");
        }
        return true;
    }

    private static boolean onlyInState(StateCarrier carrier, IRValue value) {
        if (carrier.getGlobalWriteValues().contains(value)) return false;
        if (carrier instanceof GuardBool guard && guard.getCondition() == value) return false;
        for (VirtualObjectState state : carrier.getVirtualObjects()) {
            if (carrier.getVirtualObjectValues(state).contains(value)) return false;
        }
        return true;
    }

    private static boolean isBefore(NewObject object, LoopBegin begin) {
        for (IREffect e = begin.previous; e != null; e = e.previous) {
            if (e instanceof Anchor anchor && anchor.getValue() == object) return true;
        }
        return false;
    }

    private static IREffect insertAfter(IREffect position, IREffect effect) {
        position.insertAfter(effect);
        return effect;
    }

    private static int siteOf(IREffect effect) {
        for (IREffect e = effect; e != null; e = e.previous) {
            if (e instanceof Anchor anchor) return anchor.site;
        }
        return -1;
    }

    /**
     * @return the members holding methods (index to function index in the function area of the
     * module), as created by the constructor: PUSH_THIS, LOAD_VIRTUAL f, SET_OWNER, STORE_INTERNAL m;
     * null if the class is not supported
     */
    private static Map<Integer, Integer> methodMembers(VirtualType type) {
        return methodMembers.computeIfAbsent(type, t -> {
            if (!(t.getDeclaredConstructor() instanceof VirtualFunction constructor)) return Optional.empty();
            byte[][] instructions = constructor.getMetadata().instructions();
            Map<Integer, Integer> methods = new HashMap<>();
            for (int ip = 0; ip + 3 < instructions.length; ip++) {
                if (Opcode.of(instructions[ip][0]) == Opcode.PUSH_THIS
                        && Opcode.of(instructions[ip + 1][0]) == Opcode.LOAD_VIRTUAL
                        && Opcode.of(instructions[ip + 2][0]) == Opcode.SET_OWNER
                        && Opcode.of(instructions[ip + 3][0]) == Opcode.STORE_INTERNAL) {
                    int function = Conversion.from2Bytes(instructions[ip + 1][1], instructions[ip + 1][2]);
                    int member = Conversion.from2Bytes(instructions[ip + 3][1], instructions[ip + 3][2]);
                    methods.put(member, function);
                }
            }
            return Optional.of(methods);
        }).orElse(null);
    }
}
