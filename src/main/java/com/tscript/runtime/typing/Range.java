package com.tscript.runtime.typing;

import com.tscript.runtime.utils.Tuple;


public class Range extends PrimitiveObject<Tuple<TInteger, TInteger>> {

    public static final Type TYPE =
            new Type.Builder("Range").setConstructor((thread, params) -> Null.INSTANCE).build();

    public Range(int start, int end) {
        this(new TInteger(start), new TInteger(end));
    }

    public Range(TInteger from, TInteger to) {
        super(new Tuple<>(from, to));
    }

    @Override
    public  Type getType() {
        return TYPE;
    }
}
