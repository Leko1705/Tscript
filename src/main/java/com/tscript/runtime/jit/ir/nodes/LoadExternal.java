package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class LoadExternal implements IRValue {

    public IRValue value;
    public final String name;

    public IRType type = IRUnknownType.INSTANCE;

    public LoadExternal(IRValue value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitLoadExternal(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == value) {
            value = newNode;
            return true;
        }
        return false;
    }
}
