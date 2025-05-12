package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class BinaryOp extends Binary {

    public Opcode operation;

    public IRType type = IRUnknownType.INSTANCE;

    public BinaryOp(IRValue lhs, IRValue rhs, Opcode operation) {
        super(lhs, rhs);
        this.operation = operation;
    }

    @Override
    public boolean canBePrimitive() {
        return true;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitBinaryOp(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
