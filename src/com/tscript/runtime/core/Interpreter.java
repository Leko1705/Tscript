package com.tscript.runtime.core;

import com.tscript.runtime.jit.JITSensitive;
import com.tscript.tscriptc.generation.Opcode;

public interface Interpreter {

    void pushNull();

    void pushInt(int i);

    void pushBool(boolean b);

    void pushThis();

    @JITSensitive
    void storeGlobal(int address);

    @JITSensitive
    void loadGlobal(int address);

    void storeLocal(int address);

    void loadLocal(int address);

    void loadConst(int address);

    void containerRead();

    void containerWrite();

    void returnVirtual();

    void wrapArgument(int utf8Address);

    void call(int argc);

    Data pop();

    void makeRange();

    void makeArray(int cnt);

    void makeDict(int cnt);

    void enterTry(int safeAddress);

    void leaveTry();

    void throwError();

    void jumpTo(int address);

    void getIterator();

    void iteratorNext();

    void branchIterator(int address);

    void branchOn(boolean when, int address);

    void loadMember(int utf8Address);

    void storeMember(int utf8Address);

    void loadMemberFast(int address);

    void storeMemberFast(int address);

    void compare(boolean onTrue);

    void binaryOperation(Opcode operation);

    void unaryOperation(Opcode operation);

    void getType();

    void callSuper(int argc);

    void loadAbstractMethod(int utf8Address);

    void loadStatic(int utf8Address);

    void storeStatic(int utf8Address);

    void onBreakPoint();

    void use();

    void loadName(byte b);

    @JITSensitive
    void setLine(int line);
}
