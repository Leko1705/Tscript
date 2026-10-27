package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TBoolean;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class BoolType implements IRType {

    public static final BoolType INSTANCE = new BoolType();

    private BoolType() {}

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(TBoolean.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return Boolean.TYPE;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        return type == BoolType.INSTANCE || type == UnknownType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Bool";
    }
}
