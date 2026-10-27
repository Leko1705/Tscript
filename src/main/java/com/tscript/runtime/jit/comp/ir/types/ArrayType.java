package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TArray;
import com.tscript.runtime.typing.Type;

import java.util.List;
import java.util.Set;

public class ArrayType implements ParameterizedType {

    private final IRType parameter;

    public ArrayType(IRType parameter) {
        this.parameter = parameter;
    }

    public IRType getParameter() {
        return parameter;
    }

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(TArray.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return com.tscript.runtime.typing.TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        if (type == UnknownType.INSTANCE) return true;
        if (!(type instanceof ArrayType that)) return false;
        if (this == that) return true;
        if (this.parameter == that.parameter) return true;
        return this.parameter.overlapsWith(that.parameter);
    }

    @Override
    public List<IRType> getParameters() {
        return List.of(parameter);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ArrayType a && parameter.equals(a.parameter);
    }

    @Override
    public int hashCode() {
        return 13 * parameter.hashCode() + 3;
    }

    @Override
    public String toString() {
        return "Array<" + parameter.toString() + ">";
    }
}
