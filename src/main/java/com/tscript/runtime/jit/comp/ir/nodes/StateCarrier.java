package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.FrameState;
import com.tscript.runtime.jit.comp.ir.GlobalRef;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.VirtualObjectState;

import java.util.List;

/**
 * A node that can leave compiled code (guards and exits). It carries the interpreter state to
 * restore and the globals that have to be written back to memory before leaving
 * (see global promotion).
 */
public interface StateCarrier {

    FrameState getState();

    List<IRValue> getStateValues();

    List<GlobalRef> getGlobalWrites();

    List<IRValue> getGlobalWriteValues();

    void addGlobalWrite(GlobalRef global, IRValue value);

    /**
     * @return the objects that have to be created when compiled code is left here
     */
    List<VirtualObjectState> getVirtualObjects();

    void addVirtualObject(NewObject object, int[] members, List<IRValue> values);

    /**
     * @return the values of the members of the given virtual object
     */
    default List<IRValue> getVirtualObjectValues(VirtualObjectState state) {
        int from = state.offset() + 1;
        return getInputs().subList(from, from + state.members().length);
    }

    List<IRValue> getInputs();
}
