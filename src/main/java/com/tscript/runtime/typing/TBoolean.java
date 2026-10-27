package com.tscript.runtime.typing;


import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.tni.TNIUtils;

public class TBoolean extends PrimitiveObject<Boolean> {

    public static final Type TYPE =
            Type.newBuilder("Boolean")
                    .setParameters(Parameters.newInstance().add("value", null))
                    .setConstructor((env, params) -> TBoolean.of(TNIUtils.isTrue(params.get(0))))
                    .build();

    @JITSensitive public static final TBoolean TRUE = new TBoolean(true);
    @JITSensitive public static final TBoolean FALSE = new TBoolean(false);

    @JITSensitive
    public static TBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    private TBoolean(Boolean value) {
        super(value);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

}
