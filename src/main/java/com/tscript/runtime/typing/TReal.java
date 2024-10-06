package com.tscript.runtime.typing;



public class TReal extends PrimitiveObject<Double> {

    public static final Type TYPE =
            new Type.Builder("Real")
                    .setParameters(Parameters.newInstance().add("value", null))
                    .setConstructor((thread, params) -> {
                        TObject value = params.get(0);

                        if (value instanceof TReal)
                            return value;

                        if (value instanceof TInteger integer){
                            return new TReal(integer.getValue().doubleValue());
                        }

                        if (value instanceof TString string){
                            try {
                                return new TReal(Double.parseDouble(string.getValue()));
                            }
                            catch (NumberFormatException e){
                                thread.reportRuntimeError("can not convert string '" + string.getValue() + "' to Real");
                                return null;
                            }
                        }

                        thread.reportRuntimeError("can not convert " + value.getDisplayName() + " to Real");
                        return null;
                    }).build();

    public TReal(Double value) {
        super(value);
    }

    @Override
    public  Type getType() {
        return TYPE;
    }
}
