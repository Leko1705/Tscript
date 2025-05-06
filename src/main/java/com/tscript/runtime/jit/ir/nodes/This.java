package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class This implements IRValue {

    public IRType type = IRUnknownType.INSTANCE;

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitThis(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
