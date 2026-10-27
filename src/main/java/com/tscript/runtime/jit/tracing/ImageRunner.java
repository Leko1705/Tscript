package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.ParseProcess;
import com.tscript.runtime.jit.caching.images.ImageCache;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs native images and transfers control back to the interpreter if they are left.
 * <p>
 * When compiled code is left at a guard, the side trace attached to that guard (if any) takes
 * over; if the exit is hot but no side trace exists yet, one is recorded.
 */
public final class ImageRunner {

    private ImageRunner() {
    }

    static ImageCache cache(TThread thread) {
        return thread.getVM().getJitCompiler().imageCache();
    }

    static boolean mayRunImage(TThread thread) {
        JITCompiler jit = thread.getVM().getJitCompiler();
        return jit.isEnabled() && thread.jitDepth < jit.maxImageDepth();
    }


    /* ------------------------------------------------------------------ */
    /*  function images                                                   */
    /* ------------------------------------------------------------------ */

    /**
     * Runs the image of a function called by the interpreter. The result is pushed onto the
     * frame of the caller. If the function can not be finished in compiled code, the
     * materialized frame of the callee is pushed instead, so that the interpreter continues inside it.
     */
    static void runFunction(TThread thread, ImageCache.Entry entry, NativeImage image, VirtualFunction function, TObject[] args) {
        VirtualFrame caller = thread.topVirtualFrame();
        thread.jitDepth++;
        try {
            TObject result = image.run(thread, ImageFrame.forFunction(function, args));
            if (result instanceof Deoptimization deopt) {
                Object continuation = continueFunction(thread, entry, function, deopt);
                if (continuation instanceof VirtualFrame[] frames) {
                    for (VirtualFrame frame : frames) thread.frameStack.push(frame);
                    return;
                }
                result = (TObject) continuation;
            }
            caller.push(result);
        }
        catch (ImageAbortException ex) {
            // a runtime error was reported and handled by the interpreter
        }
        finally {
            thread.jitDepth--;
        }
    }

    private static TObject runNested(TThread thread, ImageCache.Entry entry, NativeImage image, VirtualFunction function, TObject[] args) {
        thread.jitDepth++;
        try {
            TObject result = image.run(thread, ImageFrame.forFunction(function, args));
            if (result instanceof Deoptimization deopt) {
                Object continuation = continueFunction(thread, entry, function, deopt);
                if (continuation instanceof VirtualFrame[] frames) {
                    return thread.runFrames(frames);
                }
                result = (TObject) continuation;
            }
            return result;
        }
        finally {
            thread.jitDepth--;
        }
    }

    /**
     * Continues the execution of a function (whose frame is not on the frame stack) after compiled
     * code was left, using side traces as long as possible.
     * @return the return value of the function, or the materialized interpreter frames
     * ({@code VirtualFrame[]}, callers first) if the function has to be finished by the interpreter
     */
    private static Object continueFunction(TThread thread, ImageCache.Entry owner, VirtualFunction function, Deoptimization deopt) {
        TObject[] locals = new TObject[function.getLocals()];
        while (true) {
            deopt.applyTo(locals);
            NativeImage side = deopt.hasInlinedFrames() ? null : sideImage(thread, owner, deopt.info, true);
            if (side == null) {
                VirtualFrame frame = new VirtualFrame(function);
                System.arraycopy(locals, 0, frame.locals, 0, locals.length);
                deopt.applyTo(frame);
                if (!deopt.hasInlinedFrames()) return new VirtualFrame[]{frame};
                List<VirtualFrame> frames = new ArrayList<>();
                frames.add(frame);
                frames.addAll(deopt.inlinedFrames());
                return frames.toArray(new VirtualFrame[0]);
            }
            TObject result = side.run(thread, ImageFrame.forState(function, locals, deopt.rootStack()));
            if (!(result instanceof Deoptimization next)) return result;
            owner = sideEntry(thread, deopt.info);
            deopt = next;
        }
    }

    private static ImageCache.Entry sideEntry(TThread thread, DeoptInfo info) {
        Object cached = info.sideEntry;
        if (cached instanceof ImageCache.Entry entry) return entry;
        ImageCache.Entry entry = cache(thread).get(info);
        info.sideEntry = entry;
        return entry;
    }


    /* ------------------------------------------------------------------ */
    /*  loop images                                                       */
    /* ------------------------------------------------------------------ */

    /**
     * Runs the image of a loop on top of the given (top most) interpreter frame. When compiled
     * code is left, the state of the interpreter frame is updated.
     */
    static void runLoop(TThread thread, ImageCache.Entry entry, NativeImage image, VirtualFrame frame) {
        int header = frame.ip;
        thread.jitDepth++;
        try {
            while (true) {
                TObject result = image.run(thread, ImageFrame.forLoop(frame));
                if (!(result instanceof Deoptimization deopt)) {
                    throw new IllegalStateException("loop image returned regularly");
                }
                if (!continueLoop(thread, entry, frame, deopt, header)) return;

                // the loop is entered again: count it as a use (may recompile the loop, e.g. with
                // its side traces stitched in)
                ParseProcess process = entry.use();
                if (process != null) {
                    // the image was invalidated: record a new trace starting now (at the loop header)
                    if (thread.getInterpreter() instanceof Profiler profiler) profiler.startLoopTrace(thread, process, header);
                    else process.abort("can not trace");
                    return;
                }
                NativeImage current = entry.image();
                if (current == null) return;
                image = current;
            }
        }
        catch (ImageAbortException ex) {
            // a runtime error was reported and handled by the interpreter
        }
        finally {
            thread.jitDepth--;
        }
    }

    /**
     * @return true if execution should continue with the loop image again
     */
    private static boolean continueLoop(TThread thread, ImageCache.Entry owner, VirtualFrame frame, Deoptimization deopt, int header) {
        while (true) {
            deopt.applyTo(frame);
            if (deopt.hasInlinedFrames()) {
                for (VirtualFrame inlined : deopt.inlinedFrames()) thread.frameStack.push(inlined);
                return false;
            }
            if (deopt.info.loopExit()) return false;
            if (deopt.info.jumpExit()) return deopt.info.state().ip() == header;

            NativeImage side = sideImage(thread, owner, deopt.info, false);
            if (side == null) return false;
            TObject result = side.run(thread, ImageFrame.forLoop(frame));
            if (!(result instanceof Deoptimization next)) {
                throw new IllegalStateException("side trace of a loop returned regularly");
            }
            owner = sideEntry(thread, deopt.info);
            deopt = next;
        }
    }


    /* ------------------------------------------------------------------ */
    /*  side traces                                                       */
    /* ------------------------------------------------------------------ */

    /**
     * Looks up the side trace attached to the given exit. If the exit is hot and has no side
     * trace yet, recording of a side trace starts: the trace begins as soon as the interpreter
     * executes the frame that was left (it must be pushed by the caller).
     *
     * @param frameOffStack true if the frame is not (yet) on the frame stack
     * @return the image of the side trace or null if the interpreter has to continue
     */
    private static NativeImage sideImage(TThread thread, ImageCache.Entry owner, DeoptInfo info, boolean frameOffStack) {
        if (info.jumpExit()) return null;

        ImageCache.Entry side = info.sideTraceable() && owner != null ? sideEntry(thread, info) : null;
        if (side == null || side.isDisabled()) {
            if (owner != null) owner.onDeopt(info.loopExit());
            return null;
        }

        if (info.loopExit() && !frameOffStack) {
            return null;
        }

        if (thread.getInterpreter() instanceof Profiler profiler && mayRunImage(thread)) {
            ParseProcess process = side.use();
            if (process != null) {
                int depth = thread.frameStack.size() + (frameOffStack ? 1 : 0);
                profiler.startSideTrace(thread, process, info, depth, frameOffStack);
                return null;
            }
            return side.image();
        }
        return null;
    }


    /* ------------------------------------------------------------------ */
    /*  calls from compiled code                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Calls a callable from compiled code and returns its result.
     * @throws ImageAbortException if a runtime error was reported during the call
     */
    @JITSensitive
    public static TObject invoke(TThread thread, ImageFrame caller, int site, TObject callee, TObject[] args) {
        int depth = thread.frameStack.size();
        int mark = caller.enter(thread, site);
        TObject result;
        try {
            if (callee instanceof VirtualFunction function) {
                result = invokeVirtual(thread, function, args);
            }
            else if (callee instanceof Callable callable) {
                result = thread.call(callable, Arrays.asList(args));
            }
            else {
                thread.reportRuntimeError(InternalRuntimeErrorMessages.notCallable(callee));
                throw ImageAbortException.INSTANCE;
            }
        }
        finally {
            caller.leave(thread, mark);
        }

        if (result == null || !thread.isRunning() || thread.frameStack.size() != depth) {
            throw ImageAbortException.INSTANCE;
        }
        return result;
    }

    private static TObject invokeVirtual(TThread thread, VirtualFunction function, TObject[] args) {
        if (mayRunImage(thread)
                && thread.getInterpreter() instanceof Profiler profiler
                && function.getParameters(thread).count() == args.length) {

            ImageCache.Entry entry = cache(thread).get(function.getMetadata(), args);
            if (entry != null) {
                ParseProcess process = entry.use();
                if (process != null) {
                    profiler.startFunctionTrace(thread, process);
                    return thread.call(function, Arrays.asList(args));
                }
                NativeImage image = entry.image();
                if (image != null) {
                    return runNested(thread, entry, image, function, args);
                }
            }
        }
        return thread.call(function, Arrays.asList(args));
    }

}
