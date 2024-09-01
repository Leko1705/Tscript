package com.tscript.runtime.tni;

import com.tscript.runtime.core.ExecutionException;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;


public abstract class NativeFunction extends Function implements Cloneable {


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
    public final TObject eval(TThread thread, List<TObject> params) {
        return evaluate(thread, params);
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
}
