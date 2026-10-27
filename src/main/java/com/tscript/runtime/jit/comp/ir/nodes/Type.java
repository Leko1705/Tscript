package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.typing.TObject;

public class Type extends IRValue {

    private final int address;

    public Type(int address) {
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
        return visitor.visitType(this, p);
    }

    @Override
    public String toString() {
        return ((TObject)ObjectCache.getObject(address)).getDisplayName();
    }
}
