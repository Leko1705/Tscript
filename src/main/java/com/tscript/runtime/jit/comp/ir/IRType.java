package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.typing.*;

import java.util.*;

public interface IRType {

    Set<Type> getTTypeOptions();

    Class<?> getJvmType();

    default boolean isDisjoinedFrom(IRType type) {
        return !overlapsWith(type);
    }

    boolean overlapsWith(IRType type);


    static IRType from(TObject obj, boolean resolveTypeParameters) {
        Type type = obj.getType();
        if (type instanceof NullType) return NullType.INSTANCE;
        if (type == TInteger.TYPE) return IntType.INSTANCE;
        if (type == TReal.TYPE) return RealType.INSTANCE;
        if (type == TBoolean.TYPE) return BoolType.INSTANCE;
        if (type == TString.TYPE) return StrType.INSTANCE;
        if (type == Range.TYPE) return RangeType.INSTANCE;
        if (obj instanceof Type) return new TypeMirror(UnknownType.INSTANCE);
        if (obj instanceof TArray array) {
            if (!resolveTypeParameters) return new ArrayType(UnknownType.INSTANCE);
            return array.getValue().stream()
                    .map( o -> from(o, true))
                    .reduce(UnionType::mixup)
                    .map(ArrayType::new).orElseGet(() -> new ArrayType(UnknownType.INSTANCE));
        }
        if (obj instanceof TDictionary dict) {
            if (!resolveTypeParameters || dict.getValue().isEmpty()) {
                return new DictType(UnknownType.INSTANCE, UnknownType.INSTANCE);
            }
            Iterator<TObject> iter = dict.iterator();
            List<IRType> keyTypes = new ArrayList<>();
            List<IRType> valueTypes = new ArrayList<>();
            while (iter.hasNext()) {
                TObject key = iter.next();
                TObject value = dict.getValue().get(key);
                keyTypes.add(from(value, true));
                valueTypes.add(from(value, false));
            }
            return  new DictType(UnionType.mixup(keyTypes), UnionType.mixup(valueTypes));
        }
        return new ComplexType(type);
    }

}
