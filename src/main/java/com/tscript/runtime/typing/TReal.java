package com.tscript.runtime.typing;



public class TReal extends PrimitiveObject<Double> {

    public static final Type TYPE =
            new Type.Builder("Real").setConstructor((thread, params) -> Null.INSTANCE).build();

    public TReal(Double value) {
        super(value);
    }

    @Override
    public  Type getType() {
        return TYPE;
    }
}
