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

    public static Diagnostics.Error alreadyDefinedError(String name, Location location){
        return new Diagnostics.Error("'" + name + "' already defined", location, Phase.CHECKING);
    }

    public static Diagnostics.Error constantMustBeInitialized(Location location){
        return new Diagnostics.Error("constant must be initialized", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotBreakOutOfLoop(Location location){
        return new Diagnostics.Error("can not break out of loop", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotContinueOutOfLoop(Location location){
        return new Diagnostics.Error("can not continue out of loop", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotReturnOutOfFunction(Location location){
        return new Diagnostics.Error("can not return out of Function", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotThisOutOfClassOrFunction(Location location){
        return new Diagnostics.Error("can not use 'this' out of class or function", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotSuperOutOfClass(Location location){
        return new Diagnostics.Error("can not use 'super' out of class", location, Phase.CHECKING);
    }

    public static Diagnostics.Error notIterable(String type, Location location){
        return new Diagnostics.Error("<" + type + "> is not iterable", location, Phase.CHECKING);
    }

    public static Diagnostics.Error notAccessible(String type, Location location){
        return new Diagnostics.Error("<" + type + "> is not accessible", location, Phase.CHECKING);
    }

    public static Diagnostics.Error notCallable(String type, Location location){
        return new Diagnostics.Error("<" + type + "> is not callable", location, Phase.CHECKING);
    }

    public static Diagnostics.Error requiredButGotType(List<String> requiredType, String gotType, Location location){
        StringBuilder sb = new StringBuilder();
        for (String type : requiredType)
            sb.append("<").append(type).append("> ");
        return new Diagnostics.Error(sb + "expected but got <" + gotType + ">", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotOperate(String left, String right, Operation operation, Location location){
        String msg = canNotOperateMessage(left, right, operation);
        return new Diagnostics.Error(msg, location, Phase.CHECKING);
    }

    private static String canNotOperateMessage(String left, String right, Operation operation){
        return "can not perform '" + operation.encoding + "' on <" + left + "> and <" + right + ">";
    }

    public static Diagnostics.Error canNotFindSymbol(String name, Location location){
        return new Diagnostics.Error("can not find symbol '" + name + "'", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotFindClass(String name, Location location) {
        return new Diagnostics.Error("can not find class '" + name + "'", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotAccessFromStaticContext(Location location) {
        return new Diagnostics.Error("can not access non static member from static context", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotUseThisFromStaticContext(Location location) {
        return new Diagnostics.Error("can not use keyword 'this' from static context", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotUseSuperFromStaticContext(Location location) {
        return new Diagnostics.Error("can not use keyword 'super' from static context", location, Phase.CHECKING);
    }

    public static Diagnostics.Error missingDigitOnRadixSpecs(Location location){
        return new Diagnostics.Error("missing digit while using radix on integer", location, Phase.PARSING);
    }

    public static Diagnostics.Error invalidDigitOnRadixSpecs(Location location){
        return new Diagnostics.Error("invalid digit while using radix on integer", location, Phase.PARSING);
    }

    public static Diagnostics.Error invalidFraction(Location location){
        return new Diagnostics.Error("invalid fraction", location, Phase.PARSING);
    }

    public static Diagnostics.Error missingSymbol(Location location, String s) {
        return new Diagnostics.Error("missing symbol '" + s + "'", location, Phase.PARSING);
    }

    public static Diagnostics.Error invalidEscapeCharacter(Location location) {
        return new Diagnostics.Error("invalid escape character", location, Phase.PARSING);
    }

    public static Diagnostics.Error unexpectedToken(Location location, char c) {
        return new Diagnostics.Error("unexpected token '" + c + "'", location, Phase.PARSING);
    }

    public static Diagnostics.Error canNotReturnFromConstructor(Location location) {
        return new Diagnostics.Error("can not return from constructor", location, Phase.CHECKING);
    }

    public static Diagnostics.Error noSuperClassFound(Location location, String currentClassName) {
        return new Diagnostics.Error("can out use 'super' because '" + currentClassName + "' has no super class", location, Phase.CHECKING);
    }

    public static Diagnostics.Error noSuchMemberFound(Location location, String className, String memberName){
        return new Diagnostics.Error("<" + className + "> has no member '" + memberName + "'", location, Phase.CHECKING);
    }

    public static Diagnostics.Error memberIsNotVisible(Location location, Modifier givenVisibility, String memberName) {
        return new Diagnostics.Error("member '" + memberName + "' has " + givenVisibility.name + " access", location, Phase.CHECKING);
    }

    public static Diagnostics.Error hasInfiniteInheritance(Location location) {
        return new Diagnostics.Error("infinite inheritance cycle", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotDefineOutOfAbstractClass(Location location) {
        return new Diagnostics.Error("can not define abstract function out of abstract class", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotUsePrivateOnAbstract(Location location) {
        return new Diagnostics.Error("abstract function must not be private", location, Phase.CHECKING);
    }

    public static Diagnostics.Error canNotUseStaticOnAbstract(Location location) {
        return new Diagnostics.Error("abstract method must not be static", location, Phase.CHECKING);
    }
}
