package com.tscript.runtime.jit;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.VirtualFrame;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.jit.tracing.ImageAbortException;
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
     * Must be called before compiled code performs an operation that may report a runtime error
     * (e.g. calling a function): updates the line of the underlying interpreter frame, or, if
     * there is none, registers a shadow frame so that error messages list this function.
     * @return a mark to pass to {@link #leave(TThread, int)}
     */
    @JITSensitive
    public int enter(TThread thread, int line) {
        if (frame != null) {
            frame.setLine(line);
            return -1;
        }
        return thread.pushShadowFrame(metadata, line);
    }

    /**
     * Counterpart of {@link #enter(TThread, int)}.
     * @throws ImageAbortException if a runtime error was reported and unwound the stack past
     * this (frame less) compiled code. The interpreter continues where the error was caught.
     */
    @JITSensitive
    public void leave(TThread thread, int mark) {
        if (mark < 0) return;
        boolean unwound = thread.shadowFrameCount() <= mark;
        thread.popShadowFrames(mark);
        if (unwound) throw ImageAbortException.INSTANCE;
    }

}
