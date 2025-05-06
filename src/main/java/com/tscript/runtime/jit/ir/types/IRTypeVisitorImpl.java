package com.tscript.runtime.jit.ir.types;

public class IRTypeVisitorImpl<P, R> implements IRTypeVisitor<P, R> {
    
    @Override
    public R visitIntegerType(IRIntegerType integerType, P p) {
        return defaultOperation(integerType, p);
    }

    @Override
    public R visitRealType(IRRealType realType, P p) {
        return defaultOperation(realType, p);
    }

    @Override
    public R visitBooleanType(IRBooleanType booleanType, P p) {
        return defaultOperation(booleanType, p);
    }

    @Override
    public R visitStringType(IRStringType stringType, P p) {
        return defaultOperation(stringType, p);
    }

    @Override
    public R visitNullType(IRNullType nullType, P p) {
        return defaultOperation(nullType, p);
    }

    @Override
    public R visitArrayType(IRArrayType arrayType, P p) {
        return defaultOperation(arrayType, p);
    }

    @Override
    public R visitDictionaryType(IRDictionaryType dictionaryType, P p) {
        return defaultOperation(dictionaryType, p);
    }

    @Override
    public R visitRangeType(IRRangeType rangeType, P p) {
        return defaultOperation(rangeType, p);
    }

    @Override
    public R visitFunctionType(IRFunctionType functionType, P p) {
        return defaultOperation(functionType, p);
    }

    @Override
    public R visitTypeType(IRTypeType typeType, P p) {
        return defaultOperation(typeType, p);
    }

    @Override
    public R visitUnknownType(IRUnknownType unknownType, P p) {
        return defaultOperation(unknownType, p);
    }
    
    public R defaultOperation(IRType type, P p) {
        throw new UnsupportedOperationException();
    }
}
