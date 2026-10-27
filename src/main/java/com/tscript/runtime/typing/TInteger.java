package com.tscript.runtime.typing;


import com.tscript.runtime.jit.JITSensitive;

public class TInteger extends PrimitiveObject<Integer> {

    public static final Type TYPE =
            Type.newBuilder("Integer")
                    .setParameters(Parameters.newInstance().add("value", null))
                    .setConstructor(((env, params) -> {
                        TObject value = params.get(0);

                        if (value instanceof TInteger i) {
                            return i;
                        }
                        if (value instanceof TReal real){
                            return new TInteger(real.value.intValue());
                        }
                        if (value instanceof TString string){
                            try {
                                return new TInteger(Integer.parseInt(string.getValue()));
                            }
                            catch (NumberFormatException e){
                                env.reportRuntimeError("can not convert string '" + string.getValue() + "' to Integer");
                                return null;
                            }
                        }

                        env.reportRuntimeError("can not convert " + value.getDisplayName() + " to Integer");
                        return null;
                    })).build();

    @JITSensitive
    public TInteger(int value) {
        super(value);
    }

    @Override
    public Type getType() {
        return TYPE;
    }
}
