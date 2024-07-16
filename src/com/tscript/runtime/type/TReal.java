package com.tscript.runtime.type;

public class TReal extends PrimitiveObject<Double> {

    private static final TType type = new TType("Real", null);

    public TReal(Double value) {
        super(value);
    }

    @Override
    public TType getType() {
        return type;
    }
}
