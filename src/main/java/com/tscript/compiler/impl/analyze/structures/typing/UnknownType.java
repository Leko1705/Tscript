package com.tscript.compiler.impl.analyze.structures.typing;

import com.tscript.compiler.source.tree.Operation;

import java.util.Map;

public class UnknownType extends Type {

    public static final UnknownType INSTANCE = new UnknownType();

    private UnknownType() {}

    @Override
    public String getName() {
        return "unknown";
    }

    @Override
    protected Type doOperate(Operation operation, Type type, Map<String, Type> typeMap) {
        return this;
    }

    @Override
    public boolean isCallable() {
        return true;
    }

    @Override
    public boolean isItemAccessible() {
        return true;
    }
}
