package com.tscript.runtime.core;

import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Visibility;

import java.util.function.BiFunction;
import java.util.function.Function;

public class InternalRuntimeErrorMessages {

    public static String notIterable(TObject object){
        return object.getType().getName() + " is not iterable";
    }

    public static String notAccessible(TObject object){
        return object.getType().getName() + " is not accessible";
    }

    public static String notWriteable(TObject object){
        return object.getType().getName() + " is not writeable";
    }

    public static String notCallable(TObject object){
        return object.getType().getName() + " is not callable";
    }

    public static String canNotFind(String name){
        return "can not find '" + name + "'";
    }

    public static String canNotFindStaticMember(String name){
        return "can not find static member '" + name + "'";
    }

    public static String invalidBinaryOperation(TObject left, TObject right, Opcode opcode) {
        BiFunction<TObject, TObject, String> template = resolveBinaryMessageTemplate(opcode);
        return template.apply(left, right);
    }

    private static BiFunction<TObject, TObject, String> resolveBinaryMessageTemplate(Opcode opcode) {
        return switch (opcode){
            case ADD -> (o1, o2) -> "can not add " + o1.getType().getName() + " and " + o2.getType().getName();
            case SUB -> (o1, o2) -> "can not subtract " + o2.getType().getName() + " from " + o1.getType().getName();
            case MUL -> (o1, o2) -> "can not multiply " + o1.getType().getName() + " with " + o2.getType().getName();
            case DIV -> (o1, o2) -> "can not divide " + o1.getType().getName() + " by " + o2.getType().getName();
            case IDIV -> (o1, o2) -> "can not apply integer division on " + o1.getType().getName() + " and " + o2.getType().getName();
            case MOD -> (o1, o2) -> "can not apply modulo operation on " + o1.getType().getName() + " and " + o2.getType().getName();
            case POW -> (o1, o2) -> "can not apply power operation on " + o1.getType().getName() + " and " + o2.getType().getName();
            case AND -> (o1, o2) -> "can not apply 'and' operation on " + o1.getType().getName() + " and " + o2.getType().getName();
            case OR -> (o1, o2) -> "can not apply 'or' operation on " + o1.getType().getName() + " and " + o2.getType().getName();
            case XOR -> (o1, o2) -> "can not apply xor operation on " + o1.getType().getName() + " and " + o2.getType().getName();
            case LT, GT, GEQ, LEQ -> (o1, o2) -> "can not numerically compare " + o1.getType().getName() + " with " + o2.getType().getName();
            case SLA, SRA, SRL -> (o1, o2) -> "can shift " + o1.getType().getName() + " by " + o2.getType().getName();
            default -> throw new UnsupportedOperationException(opcode.name());
        };
    }

    public static String invalidUnaryOperation(TObject value, Opcode opcode) {
        Function<TObject, String> template = resolveUnaryMessageTemplate(opcode);
        return template.apply(value);
    }

    private static Function<TObject, String> resolveUnaryMessageTemplate(Opcode opcode) {
        return switch (opcode){
            case NOT -> o -> "can not invert " + o.getType().getName();
            case NEG -> o -> "can not negate " + o.getType().getName();
            case POS -> o -> "can not positive " + o.getType().getName();
            default -> throw new UnsupportedOperationException(opcode.name());
        };
    }

    public static String canNotBuildRangeFrom(TObject object){
        return "can not build range from " + object.getType().getName();
    }

    public static String noSuchMember(TObject object, String memberName){
        return object.getType().getName() + " has no member '" + memberName + "'";
    }

    public static String invalidAccessVisibility(String name, Visibility actualVisibility){
        return name + "name has " + actualVisibility.name().toLowerCase() + " access";
    }

    public static String invalidMutability(String memberName) {
        return memberName + " is immutable";
    }

    public static String invalidAbstractInstantiation(String name){
        return "Type " + name + " is abstract and cannot get instantiated";
    }

    public static String noSuchAbstractImplementationFound(String methodName){
        return "can not find implementation of '" + methodName + "'";
    }

    public static String canNotUseNonModularObject(){
        return "can not use non-modular object";
    }

    public static String nameAlreadyExists(String name){
        return "name '" + name + "' already exists in this scope";
    }

    public static String canNotFindModule(String name){
        return "can not find module '" + name + "'";
    }

    public static String tooManyParameters(Callable callable) {
        return "too many arguments for function '" + callable.getName() + "'";
    }

    public static String stackOverflowError(){
        return "stackOverflowError";
    }

    public static String missingParameter(String name){
        return "missing parameter '" + name + "'";
    }

    public static String hasNoParameters(Callable callable, String parameterName) {
        return callable.getName() + " has not parameter '" + parameterName + "'";
    }

    public static String parameterAlreadyAssigned(String parameterName){
        return "'" + parameterName + "' is already assigned";
    }

    public static String typeExpected(String expectedType, String actualType){
        return "expected type " + expectedType + " but got " + actualType;
    }

    public static String noSuchNativeFunctionFound(String name) {
        return "can not find native implementation of function '" + name + "'";
    }
}
