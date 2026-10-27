package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.ParseProcess;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.images.ImageCache;
import com.tscript.runtime.typing.TObject;

public class Profiler extends InterpreterDecorator {

    public Profiler(Interpreter interpreter) {
        super(interpreter);
    }

    @Override
    public void callInplace(byte argCount) {
        TThread thread = getCurrentThread();
        VirtualFrame frame = thread.topVirtualFrame();
        TObject top = frame.pop();

        if (!(top instanceof VirtualFunction vfunc) || (argCount > vfunc.getLocals())) {
            frame.push(top);
            super.callInplace(argCount);
            return;
        }

        TObject[] args = new TObject[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = frame.pop();
        }

        ImageCache cache = thread.getVM().getJitCompiler().get(ImageCache.KEY);
        VirtualFunction.VirtualMetadata metadata = vfunc.getMetadata();

        if (!cache.isBlacklisted(metadata)) {
            ImageCache.Entry entry = cache.get(metadata, args);

            var imgFuture = entry.getImage();
            var compObservable = entry.use();

            compObservable.thenAccept(compPc -> startFunctionTracing(vfunc, compPc));

            if (imgFuture.isDone() && !compObservable.isDone()) {
                callFunctionalNativeImage(imgFuture.resultNow(), thread, vfunc, args);
                return;
            }
        }

        for (int i = argCount-1; i >= 0; i--) frame.push(args[i]);
        frame.push(top);

        super.callInplace(argCount);
    }


    private void callFunctionalNativeImage(NativeImage image,
                                           TThread thread,
                                           VirtualFunction vfunc,
                                           TObject[] args) {
        int localsCount = vfunc.getLocals();
        TObject[] locals = new TObject[localsCount];
        System.arraycopy(args, 0, locals, 0, args.length);

        ImageFrame imgFrame = new ImageFrame(vfunc, locals);
        try {
            TObject result = image.run(thread, imgFrame);
            if (result != null) {
                VirtualFrame frame = thread.topVirtualFrame();
                frame.push(result);
            }
        }
        catch (FallbackException signal){
            onFallback(signal.guardId);
        }
    }

    private void onFallback(int guardId) {
    }

    private void startFunctionTracing(VirtualFunction func, ParseProcess compPc) {
        TThread thread = getCurrentThread();

        Tracer tracer = createTracer(func.getMetadata(), 0,
                thread.frameStack.size()+1,
                func.getParameters(thread).count(), compPc);

        thread.setInterpreter(tracer);
    }

    private void startLoopTracing(Object imageKey, ParseProcess compPc) {
        TThread thread = getCurrentThread();

        Tracer tracer = createTracer(imageKey, thread.topVirtualFrame().ip,
                thread.frameStack.size(), 0, compPc);

        thread.setInterpreter(tracer);
    }

    private Tracer createTracer(Object imageKey, int ip, int frameStackSize, int parametersLeft, ParseProcess compPc) {
        return new Tracer(getUnderlying(), this, imageKey, ip, frameStackSize, parametersLeft, compPc);
    }

}
