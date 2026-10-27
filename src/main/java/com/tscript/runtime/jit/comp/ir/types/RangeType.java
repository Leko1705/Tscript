package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.typing.Range;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class RangeType implements IRType {

    public static final RangeType INSTANCE = new RangeType();

    private RangeType() {}

    @Override
    public Set<Type> getTTypeOptions() {
        return Set.of(Range.TYPE);
    }

    @Override
    public Class<?> getJvmType() {
        return TObject.class;
    }

    @Override
    public boolean overlapsWith(IRType type) {
        if (type instanceof UnionType) return type.overlapsWith(this);
        return type == RangeType.INSTANCE || type == UnknownType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Range";
    }
}
