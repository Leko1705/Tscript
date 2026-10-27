package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class NullType implements IRType {

    public static final NullType INSTANCE = new NullType();

    private NullType() {}

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(Null.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        return type == NullType.INSTANCE || type == UnknownType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Null";
    }
}
