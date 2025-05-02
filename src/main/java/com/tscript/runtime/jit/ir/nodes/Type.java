package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Type implements IRNode {

    public int address;
    public String id;

    public Type(int address) {
        this.address = address;
        this.id = null;
    }

    public Type(com.tscript.runtime.typing.Type type) {
        this.address = -1;
        this.id = type.getId();
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
