package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.nodes.NewObject;

/**
 * Describes an object whose allocation was removed (see
 * {@link com.tscript.runtime.jit.comp.optim.ObjectScalarReplacement}) as it is at a
 * {@link StateCarrier}: the object has to be created when compiled code is left there, with the
 * given members set to the given values (the other members keep their initial values).
 *
 * @param object the removed allocation
 * @param members the indices of the members that were written
 * @param offset the index of the input of the state carrier holding the object; the values of the
 *               members follow it
 */
public record VirtualObjectState(NewObject object, int[] members, int offset) {
}
