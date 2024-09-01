package com.tscript.runtime.typing;

import com.tscript.runtime.tni.TNIUtils;


public class TString extends PrimitiveObject<String> {

    public static final Type TYPE =
            new Type.Builder("String")
                    .setConstructor((thread, params) -> new TString(TNIUtils.toString(thread, params.get(0))))
                    .build();

    public TString(String value) {
        super(value);
    }

    @Override
    public  Type getType() {
        return TYPE;
    }
}
