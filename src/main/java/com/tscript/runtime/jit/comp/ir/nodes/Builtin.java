package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class Builtin extends IRValue {

    private final int address;

    public Builtin(int address) {
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitBuiltin(this, p);
    }

    @Override
    public String toString() {
        return  "Builtin(" + address + ")";
    }
}
