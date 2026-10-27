package com.tscript.runtime.typing;


import com.tscript.runtime.core.TThread;

import java.util.List;

public abstract class Function implements Callable {


    public static final Type TYPE = Type.newBuilder("Function")
            .setConstructor((thread, params) -> {

                TObject value = params.get(0);
                if (value instanceof Function fun) {
                    return fun.dup();
                }

                thread.reportRuntimeError("can not convert " + value.getType() + " to Function");
                return null;
            }).build();


    private TObject owner;

    public TObject getOwner() {
        return owner;
    }

    public void setOwner(TObject owner) {
        this.owner = owner;
    }

    public abstract Function dup();

    public abstract TObject eval0(TThread thread, List<TObject> params);

    @Override
    public TObject eval(TThread thread, List<TObject> params) {
        return eval0(thread, params);
    }

    @Override
    public  String getDisplayName() {
        return "Function<" + getName() + ">";
    }

    @Override
    public final Type getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
