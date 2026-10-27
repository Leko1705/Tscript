package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.RangeIterator;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.Set;

/**
 * The type of {@link RangeIterator}s. Compiled code iterates over them without allocations.
 */
public class RangeIteratorType implements IRType {

    public static final RangeIteratorType INSTANCE = new RangeIteratorType();

    private RangeIteratorType() {}

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(RangeIterator.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        return type == INSTANCE || type == UnknownType.INSTANCE;
    }

    @Override
    public String toString() {
        return "RangeIterator";
    }
}
