package com.tscript.runtime.jit.comp.ir;

import java.util.Arrays;

/**
 * Describes the interpreter frame state that has to be reconstructed when a guard fails
 * (deoptimization). The values themselves are inputs of the guard; this object only describes
 * where they belong.
 *
 * @param ip            the instruction the interpreter resumes at
 * @param line          the current source line
 * @param localIndices  the local variable slots that are restored (in input order)
 * @param stackSize     the number of operand stack values that are restored (bottom first)
 */
public record FrameState(int ip, int line, int[] localIndices, int stackSize) {

    @Override
    public String toString() {
        return "FrameState[ip=" + ip + ", line=" + line + ", locals=" + Arrays.toString(localIndices)
                + ", stack=" + stackSize + "]";
    }
}
