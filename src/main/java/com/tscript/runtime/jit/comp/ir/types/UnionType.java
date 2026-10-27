package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.*;
import java.util.stream.Collectors;

public record UnionType(Set<IRType> union) implements ParameterizedType {

    public static IRType mixup(Iterable<IRType> union) {
        Set<IRType> types = new HashSet<>();
        for (IRType type : union) {
            if (type instanceof UnionType u) {
                types.addAll(u.union);
            }
            else {
                types.add(type);
            }
        }
        if (types.size() == 1) {
            return types.iterator().next();
        }
        return new UnionType(types);
    }

    public static IRType mixup(IRType... union) {
        return  mixup(Arrays.asList(union));
    }

    public UnionType(IRType... union) {
        this(new HashSet<>(Arrays.asList(union)));
    }

    @Override
    public Set<Type> getTTypeOptions() {
        return union.stream().flatMap(t -> t.getTTypeOptions().stream()).collect(Collectors.toSet());
    }

    @Override
    public Class<?> getJvmType() {
        return TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type == UnknownType.INSTANCE) return true;

        Set<IRType> types;
        if (type instanceof UnionType u) {
            types = u.union;
        } else {
            types = Set.of(type);
        }

        if (this.union.contains(UnknownType.INSTANCE))  return true;
        if (types.contains(UnknownType.INSTANCE)) return true;

        for (IRType t : types) {
            if (this.union.contains(t)) return true;
            for (IRType tt : this.union) {
                if (t.overlapsWith(tt)) return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UnionType unionType = (UnionType) o;
        return Objects.equals(union, unionType.union);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(union);
    }

    @Override
    public List<IRType> getParameters() {
        return union.stream().toList();
    }

    @Override
    public String toString() {
        return "Union[" + union + "]";
    }
}
