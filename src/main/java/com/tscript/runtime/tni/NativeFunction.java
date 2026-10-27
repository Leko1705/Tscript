package com.tscript.runtime.tni;

import com.tscript.runtime.core.ExecutionException;
import com.tscript.runtime.core.NativeFrame;
import com.tscript.runtime.core.VirtualFrame;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class NativeFunction extends Function implements Cloneable {

    private static final Map<Class<?>, Metadata> metadata = new HashMap<>();


    public abstract String getName();

    public abstract Parameters doGetParameters(Environment env);

    public abstract TObject evaluate(Environment env, List<TObject> arguments);

    @Override
    public final Parameters getParameters(TThread thread) {
        return doGetParameters(thread);
    }

    @Override
    public final boolean isVirtual() {
        return false;
    }

    @Override
    public Metadata getMetadata() {
        return metadata.computeIfAbsent(getClass(), k -> new NativeMetadata(getName()));
    }

    @Override
    public final TObject eval0(TThread thread, List<TObject> params) {
        NativeFrame frame = new NativeFrame(this);
        thread.frameStack.push(frame);
        TObject result = evaluate(thread, params);
        if (result == null) return null;
        thread.frameStack.pop();
        return result;
    }

    @Override
    public Iterable<Member> getMembers() {
        return List.of();
    }

    @Override
    public Function dup() {
        return clone();
    }

    @Override
    public NativeFunction clone() {
        try {
            return (NativeFunction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ExecutionException("clone error in " + getClass().getName());
        }
    }


    private record NativeMetadata(String name) implements Metadata {

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean isVirtual() {
            return false;
        }
    }
}
