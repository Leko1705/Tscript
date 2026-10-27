package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.nodes.NewObject;

/**
 * Describes an object whose allocation was removed (see
 * {@link com.tscript.runtime.jit.comp.optim.ObjectScalarReplacement}) as it is at a
 * {@link StateCarrier}: the object has to be created when compiled code is left there, with the
 * given members set to the given values (the other members keep their initial values).
 *
 * @param object the removed allocation
 * @param members the keys (see {@link #key(int, int)}) of the members that were written
 * @param offset the index of the input of the state carrier holding the object; the values of the
 *               members follow it
 */
public record VirtualObjectState(NewObject object, int[] members, int offset) {

    private static final int DEPTH_SHIFT = 16;

    /**
     * @return the key of the member with the given index of the super object at the given depth
     * (0 for the object itself)
     */
    public static int key(int depth, int index) {
        return (depth << DEPTH_SHIFT) | index;
    }

    public static int depthOf(int key) {
        return key >>> DEPTH_SHIFT;
    }

    public static int indexOf(int key) {
        return key & ((1 << DEPTH_SHIFT) - 1);
    }
}
