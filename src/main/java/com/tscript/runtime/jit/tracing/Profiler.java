package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.ParseProcess;
import com.tscript.runtime.jit.caching.images.ImageCache;
import com.tscript.runtime.typing.TObject;

/**
 * Interpreter decorator that counts how often functions are called and loops are iterated.
 * Hot functions and loops are traced (see {@link Tracer}); once compiled, their native images
 * are executed instead of interpreting them.
 */
public class Profiler extends InterpreterDecorator {

    public Profiler(Interpreter interpreter) {
        super(interpreter);
    }

    @Override
    public void callInplace(byte argCount) {
        TThread thread = getCurrentThread();
        VirtualFrame frame = thread.topVirtualFrame();
        TObject top = frame.stack[frame.sp - 1];

        if (top instanceof VirtualFunction function
                && ImageRunner.mayRunImage(thread)
                && function.getParameters(thread).count() == argCount) {

            TObject[] args = new TObject[argCount];
            for (int i = 0; i < argCount; i++) {
                args[i] = frame.stack[frame.sp - 2 - i];
            }

            ImageCache.Entry entry = ImageRunner.cache(thread).get(function.getMetadata(), args);

            if (entry != null) {
                ParseProcess process = entry.use();
                if (process != null) {
                    startFunctionTrace(thread, process);
                    super.callInplace(argCount);
                    return;
                }

                NativeImage image = entry.image();
                if (image != null) {
                    for (int i = 0; i <= argCount; i++) frame.pop();
                    ImageRunner.runFunction(thread, entry, image, function, args);
                    return;
                }
            }
        }

        super.callInplace(argCount);
    }

    @Override
    public void jumpTo(byte b1, byte b2) {
        VirtualFrame frame = getCurrentThread().topVirtualFrame();
        int from = frame.ip - 1;
        super.jumpTo(b1, b2);
        if (frame.ip <= from) onBackEdge(frame);
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {
        VirtualFrame frame = getCurrentThread().topVirtualFrame();
        int from = frame.ip - 1;
        super.branch(b1, b2, ifTrue);
        if (frame.ip <= from) onBackEdge(frame);
    }

    private void onBackEdge(VirtualFrame frame) {
        TThread thread = getCurrentThread();
        if (frame.inUnsafeSpot() || !ImageRunner.mayRunImage(thread)) return;

        ImageCache.Entry entry = ImageRunner.cache(thread).get(new LoopKey(frame.getMetadata(), frame.ip));
        if (entry == null) return;

        ParseProcess process = entry.use();
        if (process != null) {
            thread.setInterpreter(Tracer.forLoop(getUnderlying(), this, process, frame.ip, thread.frameStack.size()));
            return;
        }

        NativeImage image = entry.image();
        if (image != null) {
            ImageRunner.runLoop(thread, entry, image, frame);
        }
    }

    /**
     * Starts tracing the function that is called next. The tracer starts recording as soon as
     * the frame of the called function executes its first instruction.
     */
    void startFunctionTrace(TThread thread, ParseProcess process) {
        thread.setInterpreter(Tracer.forFunction(getUnderlying(), this, process, thread.frameStack.size() + 1));
    }

    /**
     * Starts tracing a side trace. The tracer starts recording as soon as the frame at the
     * given depth executes the instruction at the given address.
     */
    void startSideTrace(TThread thread, ParseProcess process, int ip, int depth, boolean allowReturn) {
        thread.setInterpreter(Tracer.forSide(getUnderlying(), this, process, ip, depth, allowReturn));
    }

}
