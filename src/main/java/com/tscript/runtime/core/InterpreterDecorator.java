package com.tscript.runtime.core;

import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class InterpreterDecorator implements Interpreter {

    private final Interpreter interpreter;

    public InterpreterDecorator(Interpreter interpreter) {
        this.interpreter = interpreter;
    }


    @Override
    public TThread getCurrentThread() {
        return interpreter.getCurrentThread();
    }

    @Override
    public void reportRuntimeError(TObject message) {
        interpreter.reportRuntimeError(message);
    }

    @Override
    public TObject call(Callable called, List<TObject> arguments) {
        return interpreter.call(called, arguments);
    }

    @Override
    public void pushNull() {
        interpreter.pushNull();
    }

    @Override
    public void pushInt(byte value) {
        interpreter.pushInt(value);
    }

    @Override
    public void pushBool(byte value) {
        interpreter.pushBool(value);
    }

    @Override
    public void loadConst(byte b1, byte b2) {
        interpreter.loadConst(b1, b2);
    }

    @Override
    public void loadNative(byte b1, byte b2) {

    }

    @Override
    public void loadVirtual(byte b1, byte b2) {
        interpreter.loadVirtual(b1, b2);
    }

    @Override
    public void loadBuiltin(byte b, byte b1) {
        interpreter.loadBuiltin(b, b1);
    }

    @Override
    public void loadType(byte b1, byte b2) {
        interpreter.loadType(b1, b2);
    }

    @Override
    public void buildType(byte b1, byte b2) {
        interpreter.buildType(b1, b2);
    }

    @Override
    public void pushThis() {
        interpreter.pushThis();
    }

    @Override
    public void pop() {
        interpreter.pop();
    }

    @Override
    public void newLine(int line) {
        interpreter.newLine(line);
    }

    @Override
    public void loadGlobal(byte address) {
        interpreter.loadGlobal(address);
    }

    @Override
    public void storeGlobal(byte address) {
        interpreter.storeGlobal(address);
    }

    @Override
    public void loadLocal(byte address) {
        interpreter.loadLocal(address);
    }

    @Override
    public void storeLocal(byte address) {
        interpreter.storeLocal(address);
    }

    @Override
    public void loadExternal(byte b1, byte b2) {
        interpreter.loadExternal(b1, b2);
    }

    @Override
    public void storeExternal(byte b1, byte b2) {
        interpreter.storeExternal(b1, b2);
    }

    @Override
    public void loadInternal(byte b1, byte b2) {
        interpreter.loadInternal(b1, b2);
    }

    @Override
    public void storeInternal(byte b1, byte b2) {
        interpreter.storeInternal(b1, b2);
    }

    @Override
    public void loadSuper(byte b1, byte b2) {
        interpreter.loadSuper(b1, b2);
    }

    @Override
    public void storeSuper(byte b1, byte b2) {
        interpreter.storeSuper(b1, b2);
    }

    @Override
    public void loadStatic(byte b1, byte b2) {
        interpreter.loadStatic(b1, b2);
    }

    @Override
    public void storeStatic(byte b1, byte b2) {
        interpreter.storeStatic(b1, b2);
    }

    @Override
    public void containerRead() {
        interpreter.containerRead();
    }

    @Override
    public void containerWrite() {
        interpreter.containerWrite();
    }

    @Override
    public void loadAbstract(byte b1, byte b2) {
        interpreter.loadAbstract(b1, b2);
    }

    @Override
    public void binaryOperation(Opcode opcode) {
        interpreter.binaryOperation(opcode);
    }

    @Override
    public void not() {
        interpreter.not();
    }

    @Override
    public void negate() {
        interpreter.negate();
    }

    @Override
    public void posivate() {
        interpreter.posivate();
    }

    @Override
    public void equals(boolean shouldBeEqual) {
        interpreter.equals(shouldBeEqual);
    }

    @Override
    public void getType() {
        interpreter.getType();
    }

    @Override
    public void makeArray(byte size) {
        interpreter.makeArray(size);
    }

    @Override
    public void makeDict(byte size) {
        interpreter.makeDict(size);
    }

    @Override
    public void makeRange() {
        interpreter.makeRange();
    }

    @Override
    public void jumpTo(byte b1, byte b2) {
        interpreter.jumpTo(b1, b2);
    }

    @Override
    public void branch(byte b1, byte b2, boolean ifTrue) {
        interpreter.branch(b1, b2, ifTrue);
    }

    @Override
    public void getIterator() {
        interpreter.getIterator();
    }

    @Override
    public void branchIterator(byte b1, byte b2) {
        interpreter.branchIterator(b1, b2);
    }

    @Override
    public void iteratorNext() {
        interpreter.iteratorNext();
    }

    @Override
    public void enterTry(byte b1, byte b2) {
        interpreter.enterTry(b1, b2);
    }

    @Override
    public void leaveTry() {
        interpreter.leaveTry();
    }

    @Override
    public void throwError() {
        interpreter.throwError();
    }

    @Override
    public void callInplace(byte argCount) {
        interpreter.callInplace(argCount);
    }

    @Override
    public void callMapped(byte argCount) {
        interpreter.callMapped(argCount);
    }

    @Override
    public void toInplaceArgument() {
        interpreter.toInplaceArgument();
    }

    @Override
    public void toMappedArgument(byte b1, byte b2) {
        interpreter.toMappedArgument(b1, b2);
    }

    @Override
    public void returnFunction() {
        interpreter.returnFunction();
    }

    @Override
    public void callSuper(byte argCount) {
        interpreter.callSuper(argCount);
    }

    @Override
    public void importModule(byte b1, byte b2) {
        interpreter.importModule(b1, b2);
    }

    @Override
    public void useMembers() {
        interpreter.useMembers();
    }

    @Override
    public void use(byte b1, byte b2) {
        interpreter.use(b1, b2);
    }

    @Override
    public void loadName(byte b1, byte b2) {
        interpreter.loadName(b1, b2);
    }

    @Override
    public void setOwner() {
        interpreter.setOwner();
    }

    @Override
    public void dup() {
        interpreter.dup();
    }

    @Override
    public void extend(byte b1, byte b2) {
        interpreter.extend(b1, b2);
    }
}
