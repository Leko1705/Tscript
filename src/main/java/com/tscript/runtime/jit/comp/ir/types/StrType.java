package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TString;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class StrType implements IRType {

    public  static final StrType INSTANCE = new StrType();

    private StrType() {}

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(TString.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return String.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        return type == StrType.INSTANCE || type == UnknownType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Str";
    }
}
