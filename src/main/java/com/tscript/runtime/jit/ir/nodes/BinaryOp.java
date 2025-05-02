package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class BinaryOp extends Binary {

    public Opcode operation;

    public BinaryOp(IRNode lhs, IRNode rhs, Opcode operation) {
        super(lhs, rhs);
        this.operation = operation;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
