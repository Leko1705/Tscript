package com.tscript.runtime.typing;



public class TInteger extends PrimitiveObject<Integer> {

    public static final Type TYPE =
            new Type.Builder("Integer")
                    .setParameters(Parameters.newInstance().add("value", null))
                    .setConstructor(((thread, params) -> {
                        TObject value = params.get(0);

                        if (value instanceof TInteger)
                            return value;
                        if (value instanceof TReal real){
                            return new TInteger(real.getValue().intValue());
                        }
                        if (value instanceof TString string){
                            try {
                                return new TInteger(Integer.parseInt(string.getValue()));
                            }
                            catch (NumberFormatException e){
                                thread.reportRuntimeError("can not convert string '" + string.getValue() + "' to Integer");
                                return null;
                            }
                        }

                        thread.reportRuntimeError("can not convert " + value.getDisplayName() + " to Integer");
                        return null;
                    })).build();

    public TInteger(int value) {
        super(value);
    }

    @Override
    public Type getType() {
        return TYPE;
    }
}
