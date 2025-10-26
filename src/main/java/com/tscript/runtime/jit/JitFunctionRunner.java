package com.tscript.runtime.jit;

import com.tscript.runtime.core.Frame;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.jit.caching.CacheEntry;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.tracing.FallbackException;
import com.tscript.runtime.jit.tracing.ImageFrame;
import com.tscript.runtime.typing.TObject;

import java.util.function.Consumer;

public class JitFunctionRunner {

    public record CallResult(boolean executed, TObject value){
    }


    public static CallResult call(TThread thread,
                                  VirtualFunction func,
                                  TObject[] args,
                                  Consumer<Integer> onFallback,
                                  Consumer<String> onThresholdExceeded) {

        JITCache cache = thread.getVM().getJitCompiler().getCache();

        String key = getCallKey(func, args);
        CacheEntry entry = cache.get(key);

        if (entry.image != null){
            ImageFrame imgFrame = new ImageFrame(func, args);
            try {
                TObject result = entry.image.run(thread, imgFrame);
                return new CallResult(true, result);
            }
            catch (FallbackException signal){
                onFallback.accept(signal.guardId);
            }
        }
        else {
            entry.hotness++;
            if (entry.hotness > JITCompiler.HOTNESS_THRESHOLD) {
                onThresholdExceeded.accept(key);
            }
        }

        return new CallResult(false, null);
    }


    public static void loop(TThread thread,
                            int fallbackAddress,
                            Consumer<Integer> onFallback,
                            Consumer<String> onThresholdExceeded) {

        JITCache cache = thread.getVM().getJitCompiler().getCache();

        String key = getLoopKey(thread, fallbackAddress);
        CacheEntry entry = cache.get(key);

        if (entry.image != null){
            Frame frame = thread.frameStack.element();
            ImageFrame imgFrame = new ImageFrame(frame);
            try {
                entry.image.run(thread, imgFrame);
                frame.ip = fallbackAddress-1;
            }
            catch (FallbackException signal){
                onFallback.accept(signal.guardId);
            }
        }
        else {
            entry.hotness++;
            if (entry.hotness > JITCompiler.HOTNESS_THRESHOLD) {
                onThresholdExceeded.accept(key);
            }
        }

    }

    public static void runSideTrace(TThread thread,
                                    int guardId,
                                    Consumer<Integer> onFallback,
                                    Consumer<String> onThresholdExceeded) {

        JITCache cache = thread.getVM().getJitCompiler().getCache();

        String key = getSideTraceKey(thread, guardId);
        CacheEntry entry = cache.get(key);

        if (entry.image != null){
            Frame frame = thread.frameStack.element();
            ImageFrame imgFrame = new ImageFrame(frame);
            try {
                entry.image.run(thread, imgFrame);
            }
            catch (FallbackException signal){
                onFallback.accept(signal.guardId);
            }
        }
        else {
            entry.hotness++;
            if (entry.hotness > JITCompiler.HOTNESS_THRESHOLD) {
                onThresholdExceeded.accept(key);
            }
        }

    }


    public static String getCallKey(VirtualFunction func, TObject[] args) {
        StringBuilder contextBuilder = new StringBuilder("functional::");
        contextBuilder.append(func.getModule().getCanonicalPath()).append(":").append(func.getName());
        for (TObject argType : args) {
            contextBuilder.append(argType.getType().getId()).append(";");
        }
        return contextBuilder.toString();
    }

    public static String getLoopKey(TThread thread, int fallbackAddress) {
        Frame frame = thread.frameStack.element();
        return "loop::" + frame.getModule().getCanonicalPath() + ":" + frame.getName() + ":" + fallbackAddress;
    }

    public static String getSideTraceKey(TThread thread, int guardId) {
        Frame frame = thread.frameStack.element();
        return "sideTrace::" + frame.getModule().getCanonicalPath() + ":" + frame.getName() + ":" + guardId;
    }

}
