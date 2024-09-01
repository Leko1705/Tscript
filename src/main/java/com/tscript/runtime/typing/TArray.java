package com.tscript.runtime.typing;



import java.util.ArrayList;
import java.util.List;

public class TArray extends PrimitiveObject<List<TObject>> {

    public static final Type TYPE =
            new Type.Builder("Array").setConstructor((thread, params) -> Null.INSTANCE).build();


    public TArray(List<TObject> value) {
        super(value);
    }

    public TArray(){
        this(new ArrayList<>());
    }

    public TArray( TObject... value) {
        this(new ArrayList<>(List.of(value)));
    }

    @Override
    public  Type getType() {
        return TYPE;
    }
}
