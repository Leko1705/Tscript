package com.tscript.runtime.typing;


import com.tscript.runtime.core.TThread;

import java.util.List;

public abstract class Function implements Callable {

    public static Function empty() {
        return new Function() {
            @Override public Iterable<Member> getMembers() { return List.of(); }
            @Override public String getName() { return "Function@" + hashCode(); }
            @Override public Parameters getParameters(TThread thread) { return Parameters.EMPTY; }
            @Override public boolean isVirtual() { return false; }
            @Override public Function dup() { return this; }
            @Override public TObject eval0(TThread thread, List<TObject> params) { return Null.INSTANCE; }
        };
    }



    public static final Type TYPE = new Type.Builder("Function")
            .setConstructor((func, thread, params) -> {

                TObject value = params.get(0);
                if (value instanceof Function fun) {
                    func.underlying = fun;
                    return func;
                }

                thread.reportRuntimeError("can not convert " + value.getType() + " to Function");
                return func;
            })
            .setInstanceMaker(Function::empty).build();


    private TObject owner;

    private Function underlying;

    public TObject getOwner() {
        return owner;
    }

    public void setOwner(TObject owner) {
        this.owner = owner;
    }

    public abstract Function dup();

    public abstract TObject eval0(TThread thread, List<TObject> params);

    @Override
    public TObject call(TThread thread, List<TObject> arguments) {
        if (underlying != null) return underlying.call(thread, arguments);
        return Callable.super.call(thread, arguments);
    }

    @Override
    public TObject eval(TThread thread, List<TObject> params) {
        if (underlying != null) return underlying.eval(thread, params);
        return eval0(thread, params);
    }

    @Override
    public  String getDisplayName() {
        return "Function<" + getName() + ">";
    }

    @Override
    public final  Type getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
