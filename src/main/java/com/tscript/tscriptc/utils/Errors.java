package com.tscript.tscriptc.utils;

import com.tscript.tscriptc.tree.Modifier;
import com.tscript.tscriptc.tree.Operation;

import java.util.List;

/**
 * Helper class for fast accessing the different compile time errors.
 * @since 1.0
 * @author Lennart Köhler
 */
public class Errors {

    private Errors(){}

    public static CompileException alreadyDefinedError(String name, Location location){
        return new CompileException("'" + name + "' already defined", location, Phase.CHECKING);
    }

    public static CompileException constantMustBeInitialized(Location location){
        return new CompileException("constant must be initialized", location, Phase.CHECKING);
    }

    public static CompileException canNotBreakOutOfLoop(Location location){
        return new CompileException("can not break out of loop", location, Phase.CHECKING);
    }

    public static CompileException canNotContinueOutOfLoop(Location location){
        return new CompileException("can not continue out of loop", location, Phase.CHECKING);
    }

    public static CompileException canNotReturnOutOfFunction(Location location){
        return new CompileException("can not return out of Function", location, Phase.CHECKING);
    }

    public static CompileException canNotThisOutOfClassOrFunction(Location location){
        return new CompileException("can not use 'this' out of class or function", location, Phase.CHECKING);
    }

    public static CompileException canNotSuperOutOfClass(Location location){
        return new CompileException("can not use 'super' out of class", location, Phase.CHECKING);
    }

    public static CompileException notIterable(String type, Location location){
        return new CompileException("<" + type + "> is not iterable", location, Phase.CHECKING);
    }

    public static CompileException notAccessible(String type, Location location){
        return new CompileException("<" + type + "> is not accessible", location, Phase.CHECKING);
    }

    public static CompileException notCallable(String type, Location location){
        return new CompileException("<" + type + "> is not callable", location, Phase.CHECKING);
    }

    public static CompileException requiredButGotType(List<String> requiredType, String gotType, Location location){
        StringBuilder sb = new StringBuilder();
        for (String type : requiredType)
            sb.append("<").append(type).append("> ");
        return new CompileException(sb + "expected but got <" + gotType + ">", location, Phase.CHECKING);
    }

    public static CompileException canNotOperate(String left, String right, Operation operation, Location location){
        String msg = canNotOperateMessage(left, right, operation);
        return new CompileException(msg, location, Phase.CHECKING);
    }

    private static String canNotOperateMessage(String left, String right, Operation operation){
        return "can not perform '" + operation.encoding + "' on <" + left + "> and <" + right + ">";
    }

    public static CompileException canNotFindSymbol(String name, Location location){
        return new CompileException("can not find symbol '" + name + "'", location, Phase.CHECKING);
    }

    public static CompileException canNotFindClass(String name, Location location) {
        return new CompileException("can not find class '" + name + "'", location, Phase.CHECKING);
    }

    public static CompileException canNotAccessFromStaticContext(Location location) {
        return new CompileException("can not access non static member from static context", location, Phase.CHECKING);
    }

    public static CompileException canNotUseThisFromStaticContext(Location location) {
        return new CompileException("can not use keyword 'this' from static context", location, Phase.CHECKING);
    }

    public static CompileException canNotUseSuperFromStaticContext(Location location) {
        return new CompileException("can not use keyword 'super' from static context", location, Phase.CHECKING);
    }

    public static CompileException missingDigitOnRadixSpecs(Location location){
        return new CompileException("missing digit while using radix on integer", location, Phase.PARSING);
    }

    public static CompileException invalidDigitOnRadixSpecs(Location location){
        return new CompileException("invalid digit while using radix on integer", location, Phase.PARSING);
    }

    public static CompileException invalidFraction(Location location){
        return new CompileException("invalid fraction", location, Phase.PARSING);
    }

    public static CompileException missingSymbol(Location location, String s) {
        return new CompileException("missing symbol '" + s + "'", location, Phase.PARSING);
    }

    public static CompileException invalidEscapeCharacter(Location location) {
        return new CompileException("invalid escape character", location, Phase.PARSING);
    }

    public static CompileException unexpectedToken(Location location, char c) {
        return new CompileException("unexpected token '" + c + "'", location, Phase.PARSING);
    }

    public static CompileException canNotReturnFromConstructor(Location location) {
        return new CompileException("can not return from constructor", location, Phase.CHECKING);
    }

    public static CompileException noSuperClassFound(Location location, String currentClassName) {
        return new CompileException("can out use 'super' because '" + currentClassName + "' has no super class", location, Phase.CHECKING);
    }

    public static CompileException noSuchMemberFound(Location location, String className, String memberName){
        return new CompileException("<" + className + "> has no member '" + memberName + "'", location, Phase.CHECKING);
    }

    public static CompileException memberIsNotVisible(Location location, Modifier givenVisibility, String memberName) {
        return new CompileException("member '" + memberName + "' has " + givenVisibility.name + " access", location, Phase.CHECKING);
    }

    public static CompileException hasInfiniteInheritance(Location location) {
        return new CompileException("infinite inheritance cycle", location, Phase.CHECKING);
    }

    public static CompileException canNotDefineOutOfAbstractClass(Location location) {
        return new CompileException("can not define abstract function out of abstract class", location, Phase.CHECKING);
    }

    public static CompileException canNotUsePrivateOnAbstract(Location location) {
        return new CompileException("abstract function must not be private", location, Phase.CHECKING);
    }

    public static CompileException canNotUseStaticOnAbstract(Location location) {
        return new CompileException("abstract method must not be static", location, Phase.CHECKING);
    }
}
