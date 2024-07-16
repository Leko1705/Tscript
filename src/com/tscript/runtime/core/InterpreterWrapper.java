package com.tscript.runtime.core;

import com.tscript.tscriptc.generation.Opcode;

import java.util.Objects;

public class InterpreterWrapper implements Interpreter {

    private Interpreter interpreter;

    public InterpreterWrapper(Interpreter wrapped){
        setInterpreter(wrapped);
    }

    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = Objects.requireNonNull(interpreter);
    }

    @Override
    public void pushNull() {
        interpreter.pushNull();
    }

    @Override
    public void pushInt(int i) {
        interpreter.pushInt(i);
    }

    @Override
    public void pushBool(boolean b) {
        interpreter.pushBool(b);
    }

    @Override
    public void pushThis() {
        interpreter.pushThis();
    }

    @Override
    public void storeGlobal(int address) {
        interpreter.storeGlobal(address);
    }

    @Override
    public void loadGlobal(int address) {
        interpreter.loadGlobal(address);
    }

    @Override
    public void storeLocal(int address) {
        interpreter.storeLocal(address);
    }

    @Override
    public void loadLocal(int address) {
        interpreter.loadLocal(address);
    }

    @Override
    public void loadConst(int address) {
        interpreter.loadConst(address);
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
    public void returnVirtual() {
        interpreter.returnVirtual();
    }

    @Override
    public void wrapArgument(int utf8Address) {
        interpreter.wrapArgument(utf8Address);
    }

    @Override
    public void call(int argc) {
        interpreter.call(argc);
    }

    @Override
    public Data pop() {
        return interpreter.pop();
    }

    @Override
    public void makeRange() {
        interpreter.makeRange();
    }

    @Override
    public void makeArray(int cnt) {
        interpreter.makeArray(cnt);
    }

    @Override
    public void makeDict(int cnt) {
        interpreter.makeDict(cnt);
    }

    @Override
    public void enterTry(int safeAddress) {
        interpreter.enterTry(safeAddress);
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
    public void jumpTo(int address) {
        interpreter.jumpTo(address);
    }

    @Override
    public void getIterator() {
        interpreter.getIterator();
    }

    @Override
    public void iteratorNext() {
        interpreter.iteratorNext();
    }

    @Override
    public void branchIterator(int address) {
        interpreter.branchIterator(address);
    }

    @Override
    public void branchOn(boolean when, int address) {
        interpreter.branchOn(when, address);
    }

    @Override
    public void loadMember(int utf8Address) {
        interpreter.loadMember(utf8Address);
    }

    @Override
    public void storeMember(int utf8Address) {
        interpreter.storeMember(utf8Address);
    }

    @Override
    public void loadMemberFast(int address) {
        interpreter.loadMemberFast(address);
    }

    @Override
    public void storeMemberFast(int address) {
        interpreter.storeMemberFast(address);
    }

    @Override
    public void compare(boolean onTrue) {
        interpreter.compare(onTrue);
    }

    @Override
    public void binaryOperation(Opcode operation) {
        interpreter.binaryOperation(operation);
    }

    @Override
    public void unaryOperation(Opcode operation) {
        interpreter.unaryOperation(operation);
    }

    @Override
    public void getType() {
        interpreter.getType();
    }

    @Override
    public void callSuper(int argc) {
        interpreter.callSuper(argc);
    }

    @Override
    public void loadAbstractMethod(int utf8Address) {
        interpreter.loadAbstractMethod(utf8Address);
    }

    @Override
    public void loadStatic(int utf8Address) {
        interpreter.loadStatic(utf8Address);
    }

    @Override
    public void storeStatic(int utf8Address) {
        interpreter.storeStatic(utf8Address);
    }

    @Override
    public void onBreakPoint() {
        interpreter.onBreakPoint();
    }

    @Override
    public void use() {
        interpreter.use();
    }

    @Override
    public void loadName(byte b) {
        interpreter.loadName(b);
    }

    @Override
    public void setLine(int line) {
        interpreter.setLine(line);
    }
}
