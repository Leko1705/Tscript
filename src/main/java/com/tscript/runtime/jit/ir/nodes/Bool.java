package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRBooleanType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Bool extends Literal<Boolean> {

    public final IRBooleanType type = new IRBooleanType();

    public Bool(boolean value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitBool(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
