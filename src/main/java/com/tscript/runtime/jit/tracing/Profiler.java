package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.JitFunctionRunner;
import com.tscript.runtime.jit.ir.IRBuilder;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.*;

public class Profiler extends InterpreterDecorator {


    public Profiler(Interpreter interpreter) {
        super(interpreter);
    }

    @Override
    public void callInplace(byte argCount) {
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();
        TObject top = frame.pop();

        if (!(top instanceof VirtualFunction vfunc)) {
            frame.push(top);
            super.callInplace(argCount);
            return;
        }

        TObject[] args = new TObject[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = frame.pop();
        }

        JitFunctionRunner.CallResult result =
                JitFunctionRunner.call(
                        thread,
                        vfunc,
                        args,
                        this::onFallback,
                        jitCacheKey -> startFunctionTracing(thread, jitCacheKey, vfunc, args));

        if (result.executed()) {
            if (result.value() != null)
                frame.push(result.value());
            return;
        }

        for (int i = argCount-1; i >= 0; i--) frame.push(args[i]);
        frame.push(top);

        super.callInplace(argCount);
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {

        if (!(called instanceof VirtualFunction vfunc))
            return super.call(called, arguments);

        TThread thread = getCurrentThread();
        TObject[] args = arguments.toArray(new TObject[0]);

        JitFunctionRunner.CallResult result =
                JitFunctionRunner.call(
                        thread,
                        vfunc,
                        args,
                        this::onFallback,
                        jitCacheKey -> startFunctionTracing(thread, jitCacheKey, vfunc, args));

        if (result.executed()) {
            return result.value();
        }

        return super.call(called, arguments);
    }

    @Override
    public void jumpTo(byte b1, byte b2) {
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();
        int prevIp = frame.ip;
        super.jumpTo(b1, b2);
        int curIp = frame.ip;

        if (curIp == prevIp) {
            thread.setInterpreter(getUnderlying());
        }
        else if (curIp < prevIp) {
            handleJumpBack(prevIp);
        }
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();
        int prevIp = frame.ip;
        super.branch(b1, b2, ifTrue);
        int curIp = frame.ip;

        if (curIp < prevIp) {
            handleJumpBack(prevIp);
        }
    }

    private void handleJumpBack(int jumpInstructionAddress){
        TThread thread = getCurrentThread();

        JitFunctionRunner.loop(
                thread,
                jumpInstructionAddress,
                this::onFallback,
                jitCacheKey -> startLoopTracing(thread, jitCacheKey, jumpInstructionAddress));
    }


    private void onFallback(int guardId) {
        if (true) return;

        TThread thread = getCurrentThread();
        JitFunctionRunner.runSideTrace(
                thread,
                guardId,
                this::onFallback,
                jitCacheKey -> startLoopTracing(thread, jitCacheKey, -1));
    }


    private void startFunctionTracing(TThread thread, String context, VirtualFunction func, TObject[] args) {

        Type[] types = new Type[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getType();
        }

        TracerConfiguration config =
                TracerConfiguration.createFunctionConfig(
                        context,
                        thread.frameStack.size()+1,
                        types,
                        func.getParameters(thread).count());

        Tracer tracer = createTracer(thread, config);
        thread.setInterpreter(tracer);
    }

    private void startLoopTracing(TThread thread, String context, int jumpInstructionAddress) {
        TracerConfiguration config =
                TracerConfiguration.createLoopConfig(
                        context, thread.frameStack.size(), jumpInstructionAddress);

        Tracer tracer = createTracer(thread, config);
        thread.setInterpreter(tracer);
    }

    private Tracer createTracer(TThread thread, TracerConfiguration configuration) {
        IRBuilder.LimitExceededListener l = createLimitExceededListener(thread);
        IRBuilder irBuilder = new IRBuilder(thread.frameStack.element().ip, l);
        return new Tracer(getUnderlying(), this, irBuilder, configuration);
    }


    private IRBuilder.LimitExceededListener createLimitExceededListener(TThread thread) {
        return new IRBuilder.LimitExceededListener(1000) {
            @Override
            public void onLimitExceeded() {
                thread.setInterpreter(Profiler.this);
            }
        };
    }


}
