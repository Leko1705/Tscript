package com.tscript.runtime.typing;



import java.util.LinkedHashMap;
import java.util.Map;

public class TDictionary extends PrimitiveObject<Map<TObject, TObject>> {

    public static final Type TYPE =
            new Type.Builder("Dictionary").setConstructor((thread, params) -> Null.INSTANCE).build();

    public TDictionary(Map<TObject, TObject> value) {
        super(value);
    }

    public TDictionary() {
        this(new LinkedHashMap<>());
    }

    @Override
    public  Type getType() {
        return TYPE;
    }
}
