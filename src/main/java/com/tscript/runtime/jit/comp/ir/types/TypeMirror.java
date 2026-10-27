package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.Type;

import java.util.List;
import java.util.Set;

public class TypeMirror implements ParameterizedType {

    private final IRType type;

    public TypeMirror(IRType type) {
        this.type = type;
    }

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(Type.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return Integer.TYPE;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        if (type == UnknownType.INSTANCE) return true;
        if (!(type instanceof TypeMirror that)) return false;
        if (this == that) return true;
        if (this.type == that.type) return true;
        return this.type.overlapsWith(that.type);
    }

    @Override
    public List<IRType> getParameters() {
        return List.of(type);
    }

    @Override
    public String toString() {
        return "Type<" + type.toString() + ">";
    }
}
