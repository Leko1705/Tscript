package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRStringType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Str extends Literal<String> {

    public final IRStringType type = new IRStringType();

    public Str(String value) {
        super(value);
    }

    @Override
    public boolean canBePrimitive() {
        return true;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitString(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
