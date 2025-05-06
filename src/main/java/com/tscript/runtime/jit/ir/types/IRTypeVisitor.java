package com.tscript.runtime.jit.ir.types;


public interface IRTypeVisitor<P, R> {

    R visitIntegerType(IRIntegerType integerType, P p);

    R visitRealType(IRRealType realType, P p);

    R visitBooleanType(IRBooleanType booleanType, P p);

    R visitStringType(IRStringType stringType, P p);

    R visitNullType(IRNullType nullType, P p);

    R visitArrayType(IRArrayType arrayType, P p);

    R visitDictionaryType(IRDictionaryType dictionaryType, P p);

    R visitRangeType(IRRangeType rangeType, P p);

    R visitFunctionType(IRFunctionType functionType, P p);

    R visitTypeType(IRTypeType typeType, P p);

    R visitUnknownType(IRUnknownType unknownType, P p);

}
