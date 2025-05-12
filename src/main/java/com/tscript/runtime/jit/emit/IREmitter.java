package com.tscript.runtime.jit.emit;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.IRBlock;

public interface IREmitter {

    void init(IRBlock block);

    byte[] build();


    void emitNewLine(int line);

    void emitLoadInt(int value);

    void emitLoadDouble(double value);

    void emitLoadBoolean(boolean value);

    void emitLoadTNull();

    void emitLoadString(String value);

    void emitLoadVirtual(int address);

    void emitLoadNative(String name);

    void emitLoadBuiltin(int id);

    void emitLoadAbstract(String name);

    void emitMakeArray(int size);

    void emitMakeDictionary(int size);

    void emitMakeRange(boolean fromWrapped);

    void emitLoadType(int address, boolean fromModule);

    void emitGetType();

    void emitLoadThis();

    void emitGetIterator();

    void emitIteratorHasNext();

    void emitIteratorNext();

    void emitToInplaceArgument();

    void emitToMappedArgument(String name);

    void emitCallInplace(int argCount);

    void emitPushFrame();

    void emitPopFrame();

    void emitReturn();

    void emitWrapInt();

    void emitUnwrapInt();

    void emitWrapReal();

    void emitUnwrapReal();

    void emitWrapBoolean();

    void emitUnwrapBoolean();

    void emitWrapString();

    void emitUnwrapString();

    void emitWrapType();

    void emitUnwrapType();

    void emitWrapArray();

    void emitUnwrapArray();

    void emitWrapDictionary();

    void emitUnwrapDictionary();

    void emitInt2Double();

    void emitDouble2Int();

    void emitLabel(int label);

    void emitGoto(int label);

    void emitBranchWrapped(int jumpLabel, int nonJumpLabel);

    void emitBranchPrimitive(int jumpLabel, int nonJumpLabel);

    void emitBranchIterator(int jumpLabel, int nonJumpLabel);

    void emitGuardWrapped(int ipRecover, int spRecover);

    void emitGuardPrimitiveBoolean(int ipRecover, int spRecover);

    void emitStoreLocal(int index);

    void emitStoreLocalInt(int index);

    void emitStoreLocalDouble(double index);

    void emitStoreLocalBoolean(boolean index);

    void emitStoreLocalString(String index);

    void emitLoadLocal(int index);

    void emitLoadLocalInt(int index);

    void emitLoadLocalDouble(int index);

    void emitLoadLocalBoolean(int index);

    void emitLoadLocalString(int index);

    void emitLoadTemp(int index);

    void emitStoreTemp(int index);

    void emitLoadGlobal(int index);

    void emitStoreGlobal(int index);

    void emitLoadInternal(int index);

    void emitStoreInternal(int index);

    void emitLoadExternal(String name);

    void emitStoreExternal(String name);

    void emitLoadStatic(String name);

    void emitStoreStatic(String name);

    void emitLoadSuper(String name);

    void emitStoreSuper(String name);

    void emitLoadName(String name);

    void emitCompareDynamic(boolean shouldBeEqual);

    void emitCompareInt(boolean shouldBeEqual);

    void emitCompareDouble(boolean shouldBeEqual);

    void emitCompareBoolean(boolean shouldBeEqual);

    void emitCompareString(boolean shouldBeEqual);

    void emitOperateDynamic(Opcode operation);

    void emitAddInt();

    void emitAddDouble();

    void emitSubInt();

    void emitSubDouble();

    void emitMulInt();

    void emitMulDouble();

    void emitDivInt();

    void emitDivDouble();

    void emitModuloInt();

    void emitAndInt();

    void emitOrInt();

    void emitXorInt();

    void emitAndBool();

    void emitOrBool();

    void emitXorBool();

    void emitNotInt();

    void emitNotBool();

    void emitNegateInt();

    void emitNegateDouble();

    void emitContainerWriteDynamic();

    void emitContainerWriteArray();

    void emitContainerReadDynamic();

    void emitContainerReadArray();

    void emitExprStmtCompletion();

    void emitSetOwner();

    void emitExtend(String name);

    void emitImport(int address);

    void emitImport(String name);

    void emitUse(String name);

    void emitUseMembers();

}
