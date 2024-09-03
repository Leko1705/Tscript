package com.tscript.runtime.core;

public interface Interpreter {

    void pushNull();

    void pushInt(byte value);

    void pushBool(byte value);

    void loadConst(byte b1, byte b2);

    void loadNative(byte b1, byte b2);

    void loadVirtual(byte b1, byte b2);

    void loadType(byte b1, byte b2);

    void pushThis();

    void pop();

    void newLine(int line);

    void loadGlobal(byte address);

    void storeGlobal(byte address);

    void loadLocal(byte address);

    void storeLocal(byte address);

    void loadExternal(byte b1, byte b2);

    void storeExternal(byte b1, byte b2);

    void loadInternal(byte address);

    void storeInternal(byte address);

    void loadStatic(byte b1, byte b2);

    void storeStatic(byte b1, byte b2);

    void containerRead();

    void containerWrite();

    void loadAbstract(byte b1, byte b2);

    void binaryOperation(Opcode opcode);

    void not();

    void negate();

    void posivate();

    void equals(boolean shouldBeEqual);

    void getType();

    void makeArray(byte size);

    void makeDict(byte size);

    void makeRange();

    void jumpTo(byte b1, byte b2);

    void branch(byte b1, byte b2, boolean ifTrue);

    void getIterator();

    void branchIterator(byte b1, byte b2);

    void iteratorNext();

    void enterTry(byte b1, byte b2);

    void leaveTry();

    void throwError();

    void callInplace(byte argCount);

    void callMapped(byte argCount);

    void toInplaceArgument();

    void toMappedArgument(byte b1, byte b2);

    void returnFunction();

    void callSuper(byte argCount);

    void importModule(byte b1, byte b2);

    void use();

    void loadName(byte b1, byte b2);

    void setOwner();

    void dup();
}
