package com.tscript.runtime.typing;



public class TInteger extends PrimitiveObject<Integer> {

    public static final Type TYPE =
            new Type.Builder("Integer").setConstructor(((thread, params) -> Null.INSTANCE)).build();

    public TInteger(int value) {
        super(value);
    }

    @Override
    public Type getType() {
        return TYPE;
    }
}
