package com.tscript.runtime.typing;


import com.tscript.runtime.jit.JITSensitive;

public class Null extends PrimitiveObject<Void> {

    public static final Type TYPE =
            new Type.Builder("Null")
            .setConstructor((thread, params) -> Null.INSTANCE)
            .build();

    @JITSensitive
    public static final Null INSTANCE = new Null();

    private Null() {
        super(null);
    }

    @Override
    public  Type getType() {
        return TYPE;
    }

}
