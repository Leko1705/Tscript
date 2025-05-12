package com.tscript.runtime.jit.ir.types;

public class IRDictionaryType implements IRType {

    @Override
    public boolean isIterable() {
        return true;
    }

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitDictionaryType(this, p);
    }
}
