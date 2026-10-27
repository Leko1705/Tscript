package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TDictionary;
import com.tscript.runtime.typing.Type;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DictType implements ParameterizedType {

    private final IRType keyType;
    private final IRType valueType;

    public DictType(IRType keyType, IRType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public IRType getKeyParameter() {
        return keyType;
    }

    public IRType getValueParameter() {
        return valueType;
    }

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(TDictionary.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return com.tscript.runtime.typing.TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        if (type == UnknownType.INSTANCE) return true;
        if (!(type instanceof DictType that)) return false;
        if (this == that) return true;
        if (this.keyType == that.keyType && this.valueType == that.valueType) return true;
        return this.keyType.overlapsWith(that.keyType) && this.valueType.overlapsWith(that.valueType);
    }

    @Override
    public List<IRType> getParameters() {
        return List.of(keyType, valueType);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DictType dictType = (DictType) o;
        return Objects.equals(keyType, dictType.keyType) && Objects.equals(valueType, dictType.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyType, valueType);
    }

    @Override
    public String toString() {
        return "Dict<" + keyType.toString() + ", " + valueType.toString() + ">";
    }
}
