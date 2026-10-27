package com.tscript.runtime.jit.comp.ir;

import java.util.Arrays;

/**
 * Describes the interpreter state that has to be reconstructed when compiled code is left
 * (deoptimization). The values themselves are inputs of the guard or exit that owns the state;
 * this object only describes where they belong.
 * <p>
 * A frame state describes one interpreter frame. If code of other functions was inlined, the
 * state of the inlined frame refers to the state of its caller ({@link #caller()}); the root
 * state describes the frame the trace itself belongs to.
 * <p>
 * Values are ordered from the root frame to the innermost frame. Per frame: the owner of the
 * function (inlined frames only), the local variables, then the operand stack (bottom first).
 *
 * @param caller        the state of the calling frame or null for the root frame
 * @param metadataId    ObjectCache id of the function metadata of an inlined frame, -1 for the root frame
 * @param ip            the instruction the interpreter resumes at
 * @param line          the current source line
 * @param localIndices  the local variable slots that are restored (in value order)
 * @param stackSize     the number of operand stack values that are restored
 */
public record FrameState(FrameState caller, int metadataId, int ip, int line, int[] localIndices, int stackSize) {

    /**
     * Creates the state of a root frame.
     */
    public FrameState(int ip, int line, int[] localIndices, int stackSize) {
        this(null, -1, ip, line, localIndices, stackSize);
    }

    public boolean isRoot() {
        return caller == null;
    }

    public FrameState root() {
        FrameState state = this;
        while (state.caller != null) state = state.caller;
        return state;
    }

    /**
     * @return the number of frames described by this state (1 for a root state)
     */
    public int depth() {
        return caller == null ? 1 : caller.depth() + 1;
    }

    public int ownValueCount() {
        return (isRoot() ? 0 : 1) + localIndices.length + stackSize;
    }

    public int valueCount() {
        return (caller == null ? 0 : caller.valueCount()) + ownValueCount();
    }

    /**
     * @return the index of the first own value within all values of this state
     */
    public int offset() {
        return caller == null ? 0 : caller.valueCount();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        String own = "[ip=" + ip + ", line=" + line + ", locals=" + Arrays.toString(localIndices) + ", stack=" + stackSize + "]";
        return caller == null ? "FrameState" + own : caller + " -> inlined" + own;
    }
}
