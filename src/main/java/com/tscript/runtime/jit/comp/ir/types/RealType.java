package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TReal;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class RealType implements IRType {

    public static final RealType INSTANCE = new RealType();

    private RealType() {
    }

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(TReal.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return Double.TYPE;
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
        return  "Real";
    }
}
