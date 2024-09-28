package com.tscript.compiler.impl.utils.typing;

import com.tscript.compiler.source.tree.Operation;

import java.util.Map;

public abstract class Type {

    public abstract String getName();

    public final Type operate(Operation operation, Type type, Map<String, Type> typeMap){
        if (type == UnknownType.INSTANCE) return UnknownType.INSTANCE;
        return doOperate(operation, type, typeMap);
    }

    protected abstract Type doOperate(Operation operation, Type type, Map<String, Type> typeMap);

    public abstract boolean isCallable();

    public abstract boolean isItemAccessible();

}
