package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.*;

public class Profiler extends InterpreterDecorator {

    public static final boolean DEBUG = true;


    private record Key(Module module, String name, int pos, Type[] types) {
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

    public static class Value {
        int hotness = 0;
        NativeImage image;
    }

    private static final int TRACING_THRESHOLD = 100;

    private final Map<Key, Value> cache = new HashMap<>();

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

        Key key = new Key(vfunc.getModule(), vfunc.getName(), -1, types); // ip does not matter for function cals
        Value value = cache.computeIfAbsent(key, k -> new Value());

        if (value.image != null){
            ImageFrame imgFrame = new ImageFrame(vfunc, args);
            try {
                TObject result = value.image.run(thread, imgFrame);
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
        checkThreshold(value, argCount);
        super.callInplace(argCount);
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {
        if (called instanceof VirtualFunction vfunc) {
            TThread thread = getCurrentThread();
            Frame frame = thread.frameStack.element();

            Type[] types = new Type[arguments.size()];
            for (int i = 0; i < arguments.size(); i++)
                types[i] = arguments.get(i).getType();

            Key key = new Key(vfunc.getModule(), vfunc.getName(), frame.ip, types);
            Value value = cache.computeIfAbsent(key, k -> new Value());

            if (value.image != null) {
                ImageFrame imgFrame = new ImageFrame(vfunc, arguments.toArray(new TObject[0]));
                try {
                    return value.image.run(thread, imgFrame);
                }
                catch (FallbackException ignored){
                    if (DEBUG){
                        System.err.println("Fallback exception");
                    }
                    return null;
                }
            }
            else {
                checkThreshold(value, arguments.size());
            }
        }

        return super.call(called, arguments);
    }

    private void checkThreshold(Value value, int args) {
        value.hotness++;
        if (value.hotness >= TRACING_THRESHOLD) {
            TThread thread = getCurrentThread();
            int callStackDepth = thread.frameStack.size();

            Tracer tracer = new Tracer(
                    this.getUnderlying(),
                    this,
                    callStackDepth,
                    value,
                    args
            );

            thread.setInterpreter(tracer);
        }
    }

}
