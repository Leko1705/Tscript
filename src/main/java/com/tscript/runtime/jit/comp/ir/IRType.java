package com.tscript.runtime.jit.comp.ir;

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

    /**
     * @return true if values of this type have a dedicated (unboxed or immutable) representation
     * in compiled code: Integer, Real, Boolean, String and Null.
     */
    static boolean isPrimitive(IRType type) {
        return type == IntType.INSTANCE
                || type == RealType.INSTANCE
                || type == BoolType.INSTANCE
                || type == StrType.INSTANCE
                || type == NullType.INSTANCE;
    }

    /**
     * @return the IR type describing objects of the given runtime type
     */
    /**
     * @return true if the representation of values of this type is exactly known, i.e. a check
     * whether a value has this type can be decided statically if the value has an exact type.
     */
    static boolean isExact(IRType type) {
        return isPrimitive(type) || type == RangeIteratorType.INSTANCE;
    }

    static IRType forRuntimeType(Type type) {
        if (type == RangeIterator.TYPE) return RangeIteratorType.INSTANCE;
        if (type == Null.TYPE) return NullType.INSTANCE;
        if (type == TInteger.TYPE) return IntType.INSTANCE;
        if (type == TReal.TYPE) return RealType.INSTANCE;
        if (type == TBoolean.TYPE) return BoolType.INSTANCE;
        if (type == TString.TYPE) return StrType.INSTANCE;
        if (type == Range.TYPE) return RangeType.INSTANCE;
        if (type == TArray.TYPE) return new ArrayType(UnknownType.INSTANCE);
        if (type == TDictionary.TYPE) return new DictType(UnknownType.INSTANCE, UnknownType.INSTANCE);
        if (type == Type.TYPE) return new TypeMirror(UnknownType.INSTANCE);
        return new ComplexType(type);
    }

    /**
     * @return the runtime type of objects described by the given primitive IR type, or null
     */
    static Type runtimeTypeOf(IRType type) {
        if (type == NullType.INSTANCE) return Null.TYPE;
        if (type == IntType.INSTANCE) return TInteger.TYPE;
        if (type == RealType.INSTANCE) return TReal.TYPE;
        if (type == BoolType.INSTANCE) return TBoolean.TYPE;
        if (type == StrType.INSTANCE) return TString.TYPE;
        if (type == RangeType.INSTANCE) return Range.TYPE;
        if (type == RangeIteratorType.INSTANCE) return RangeIterator.TYPE;
        if (type instanceof ArrayType) return TArray.TYPE;
        if (type instanceof DictType) return TDictionary.TYPE;
        if (type instanceof ComplexType c) return c.getType();
        return null;
    }


    static IRType from(TObject obj, boolean resolveTypeParameters) {
        Type type = obj.getType();
        if (!resolveTypeParameters) return forRuntimeType(type);
        if (type == Null.TYPE) return NullType.INSTANCE;
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
                keyTypes.add(from(key, true));
                valueTypes.add(from(value, true));
            }
            return  new DictType(UnionType.mixup(keyTypes), UnionType.mixup(valueTypes));
        }
        return new ComplexType(type);
    }

}
