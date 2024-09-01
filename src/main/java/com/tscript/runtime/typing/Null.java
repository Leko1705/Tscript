package com.tscript.runtime.typing;



public class Null extends PrimitiveObject<Void> {

    public static final Type TYPE =
            new Type.Builder("Null")
            .setConstructor((thread, params) -> Null.INSTANCE)
            .build();

    public static final Null INSTANCE = new Null();

    private Null() {
        super(null);
    }

    @Override
    public  Type getType() {
        return TYPE;
    }

}
