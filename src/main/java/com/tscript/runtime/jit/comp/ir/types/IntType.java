package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class IntType implements IRType {

    public static final IntType INSTANCE = new IntType();

    private IntType() {
    }

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(TInteger.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return Integer.TYPE;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        return type == IntType.INSTANCE
                || type == RealType.INSTANCE
                || type == UnknownType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Int";
    }
}
