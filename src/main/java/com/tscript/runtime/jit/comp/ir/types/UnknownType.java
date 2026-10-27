package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class UnknownType implements IRType {

    public static final UnknownType INSTANCE = new UnknownType();

    private UnknownType() {}

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of();
    }

    @Override
    public Class<?> getJvmType() {
        return TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        return true;
    }

    @Override
    public String toString() {
        return "UNKNOWN";
    }

}
