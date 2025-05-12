package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRIntegerType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Int extends Literal<Integer> {

    public final IRIntegerType type = new IRIntegerType();

    public Int(int value) {
        super(value);
    }

    @Override
    public boolean canBePrimitive() {
        return true;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitInteger(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
