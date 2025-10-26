package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class ItrNext implements IRValue {

    public IRType type = IRUnknownType.INSTANCE;

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitItrNext(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }

    @Override
    public boolean isCallingInstruction() {
        return true;
    }
}
