package com.tscript.runtime.jit;

import com.tscript.runtime.core.VirtualFunction;

/**
 * A source position in compiled code: the line in the frame of the trace and, if code of other
 * functions was inlined, the inlined functions and their current lines (outermost first).
 * Used to produce exact error messages for errors that occur while compiled code runs.
 */
public record SourceSite(int rootLine, VirtualFunction.VirtualMetadata[] inlined, int[] inlinedLines) {

    public static final SourceSite UNKNOWN = new SourceSite(-1, new VirtualFunction.VirtualMetadata[0], new int[0]);

    public static SourceSite of(int line) {
        return new SourceSite(line, UNKNOWN.inlined, UNKNOWN.inlinedLines);
    }
}
