package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.ir.IRBuilder;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;
import com.tscript.runtime.stroage.Module;

import java.util.*;

public class Profiler extends InterpreterDecorator {

    public static final boolean DEBUG = false;


    private record Key(Module module, String name, int pos, Type[] types) {

        public Key(Module module, String name, Type[] types) {
            this(module, name, -1, types);
        }

        public Key(Module module, String name, int ip) {
            this(module, name, ip, null);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return pos == key.pos
                    && Objects.equals(name, key.name)
                    && Objects.equals(module, key.module)
                    && Arrays.equals(types, key.types);
        }
        @Override
        public int hashCode() {
            return Objects.hash(module, name, pos);
        }

    }

    private static final int TRACING_THRESHOLD = 100;


    private final Map<Key, Integer> hotnessCache = new HashMap<>();

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
        Type[] types = new Type[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = frame.pop();
            types[i] = args[i].getType();
        }

        String context = vfunc.getModule().getCanonicalPath() + ":" + vfunc.getName();
        NativeImage image = cache().getFunctional(context, types);

        if (image != null){
            ImageFrame imgFrame = new ImageFrame(vfunc, args);
            try {
                TObject result = image.run(thread, imgFrame);
                if (result != null) {
                    frame.push(result);
                }
            }
            catch (FallbackException ignored){
                if (DEBUG){
                    System.err.println("Fallback exception");
                }
            }
            return;
        }

        for (int i = argCount-1; i >= 0; i--) frame.push(args[i]);
        frame.push(top);

        Key key = new Key(vfunc.getModule(), vfunc.getName(), types);

        TracerConfiguration config =
                new FunctionTracingConfiguration(
                        context,
                        types,
                        thread.frameStack.size(),
                        vfunc.getParameters(thread).count());

        checkThreshold(key, config);

        super.callInplace(argCount);
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {

        if (!(called instanceof VirtualFunction vfunc))
            return super.call(called, arguments);


        TThread thread = getCurrentThread();

        Type[] types = new Type[arguments.size()];
        for (int i = 0; i < arguments.size(); i++)
            types[i] = arguments.get(i).getType();

        String context = vfunc.getModule().getCanonicalPath() + ":" + vfunc.getName();
        NativeImage image = cache().getFunctional(context, types);

        if (image != null) {
            ImageFrame imgFrame = new ImageFrame(vfunc, arguments.toArray(new TObject[0]));
            try {
                return image.run(thread, imgFrame);
            }
            catch (FallbackException ignored){
                if (DEBUG){
                    System.err.println("Fallback exception");
                }
                return null;
            }
        }
        else {
            Key key = new Key(vfunc.getModule(), vfunc.getName(), types);

            TracerConfiguration config =
                    new FunctionTracingConfiguration(
                            context,
                            types,
                            thread.frameStack.size(),
                            vfunc.getParameters(thread).count());

            checkThreshold(key, config);
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
            // handleJumpBack(curIp+1);
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
            // handleJumpBack(prevIp);
        }
    }

    private void handleJumpBack(int jumpBackAddress){
        TThread thread = getCurrentThread();
        Frame frame = thread.frameStack.element();

        String context = frame.getModule().getCanonicalPath() + ":" + frame.getName();
        NativeImage image = cache().getPositional(context, jumpBackAddress);

        if (image != null) {
            ImageFrame imgFrame = new ImageFrame(frame);
            try {
                TObject res = image.run(thread, imgFrame);
                if (res != null) {
                    frame.push(res);
                }
                frame.ip = jumpBackAddress-1;
            }
            catch (FallbackException ignored){
                if (DEBUG){
                    System.err.println("Fallback exception");
                }
            }
        }
        else {
            Key key = new Key(frame.getModule(), frame.getName(), jumpBackAddress);

            TracerConfiguration config =
                    new LoopTracingConfiguration(
                            context,
                            thread.frameStack.size(),
                            jumpBackAddress);

            checkThreshold(key, config);
        }
    }

    private void checkThreshold(Key key, TracerConfiguration configuration) {
        int hotness = hotnessCache.computeIfAbsent(key, k -> 0);
        hotness++;
        hotnessCache.put(key, hotness);

        if (hotness >= TRACING_THRESHOLD) {
            TThread thread = getCurrentThread();
            Tracer2 tracer2 = getTracer2(configuration, thread);
            thread.setInterpreter(tracer2);
        }
    }

    private Tracer2 getTracer2(TracerConfiguration configuration, TThread thread) {

        IRBuilder.LimitExceededListener l = new IRBuilder.LimitExceededListener(1000) {
            @Override
            public void onLimitExceeded() {
                thread.setInterpreter(Profiler.this);
            }
        };

        IRBuilder irBuilder = new IRBuilder(thread.frameStack.element().ip, l);

        return new Tracer2(
                getUnderlying(),
                this,
                irBuilder,
                configuration);
    }

    private JITCache cache() {
        return getCurrentThread().getVM().getJitCompiler().getCache();
    }

}
