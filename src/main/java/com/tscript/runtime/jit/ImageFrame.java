package com.tscript.runtime.jit;

import com.tscript.runtime.core.VirtualFrame;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.typing.TObject;

/**
 * The execution context handed to a {@link NativeImage}.
 * <ul>
 *     <li>Images of function traces get the call arguments ({@link #args}) and run without an
 *     interpreter frame. If they deoptimize, a new interpreter frame is materialized.</li>
 *     <li>Images of loop traces run on top of the interpreter frame the loop belongs to
 *     ({@link #frame}). If they deoptimize, the state is written back into that frame.</li>
 * </ul>
 */
public class ImageFrame {

    @JITSensitive public final VirtualFunction.VirtualMetadata metadata;
    @JITSensitive public final TObject owner;
    @JITSensitive public final TObject[] args;
    @JITSensitive public final TObject[] locals;
    @JITSensitive public final TObject[] stack;
    @JITSensitive public final VirtualFrame frame;

    private ImageFrame(VirtualFunction.VirtualMetadata metadata, TObject owner, TObject[] args,
                       TObject[] locals, TObject[] stack, VirtualFrame frame) {
        this.metadata = metadata;
        this.owner = owner;
        this.args = args;
        this.locals = locals;
        this.stack = stack;
        this.frame = frame;
    }

    /**
     * Context of a function trace: the trace starts with the given arguments.
     */
    public static ImageFrame forFunction(VirtualFunction function, TObject[] args) {
        return new ImageFrame(function.getMetadata(), function.getOwner(), args, null, null, null);
    }

    /**
     * Context of a trace running on top of an interpreter frame.
     */
    public static ImageFrame forLoop(VirtualFrame frame) {
        return new ImageFrame(frame.getMetadata(), frame.getOwner(), null, frame.locals, frame.stack, frame);
    }

    /**
     * Context of a trace that starts at the given (not yet materialized) frame state.
     */
    public static ImageFrame forState(VirtualFunction function, TObject[] locals, TObject[] stack) {
        return new ImageFrame(function.getMetadata(), function.getOwner(), null, locals, stack, null);
    }

    /**
     * Updates the line of the underlying interpreter frame (if there is one), so that error
     * messages produced while running compiled code point to the right line.
     */
    @JITSensitive
    public void syncLine(int line) {
        if (frame != null) frame.setLine(line);
    }

}
