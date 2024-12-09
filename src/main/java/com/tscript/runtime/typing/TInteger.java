package com.tscript.runtime.typing;


import com.tscript.runtime.jit.JITSensitive;

public class TInteger extends PrimitiveObject<Integer> {

    public static final Type TYPE =
            new Type.Builder("Integer")
                    .setParameters(Parameters.newInstance().add("value", null))
                    .setInstanceMaker(() -> new TInteger(0))
                    .setConstructor(((func, thread, params) -> {
                        TObject value = params.get(0);
                        TInteger owner = (TInteger) func.getOwner();

                        if (value instanceof TInteger i) {
                            owner.value = i.value;
                            return owner;
                        }
                        if (value instanceof TReal real){
                            owner.value = real.value.intValue();
                            return owner;
                        }
                        if (value instanceof TString string){
                            try {
                                owner.value = Integer.parseInt(string.getValue());
                                return owner;
                            }
                            catch (NumberFormatException e){
                                thread.reportRuntimeError("can not convert string '" + string.getValue() + "' to Integer");
                                return null;
                            }
                        }

                        thread.reportRuntimeError("can not convert " + value.getDisplayName() + " to Integer");
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
