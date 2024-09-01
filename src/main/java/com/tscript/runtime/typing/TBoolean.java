package com.tscript.runtime.typing;



public class TBoolean extends PrimitiveObject<Boolean> {

    public static final Type TYPE =
            new Type.Builder("Boolean").setConstructor((thread, params) -> Null.INSTANCE).build();

    public static final TBoolean TRUE = new TBoolean(true);
    public static final TBoolean FALSE = new TBoolean(false);


    public static TBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }


    private TBoolean(Boolean value) {
        super(value);
    }

    @Override
    public  Type getType() {
        return TYPE;
    }

}
