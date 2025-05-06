package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

import java.util.Objects;

public class ImportModule implements IRValue {

    public int index;
    public String name;

    public ImportModule(int index) {
        this.index = index;
        this.name = null;
    }

    public ImportModule(String name) {
        this.index = -1;
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitImportModule(this, p);
    }

    @Override
    public IRType getType() {
        return IRUnknownType.INSTANCE;
    }
}
