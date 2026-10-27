package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class ComplexType implements IRType {

    private final Type type;

    public ComplexType(Type type) {
        this.type = type;
    }

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(type);
    }

    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getJvmType() {
        return TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        return type instanceof ComplexType c && type == c.type || type == UnknownType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Complex<" + type.toString() + ">";
    }
}
