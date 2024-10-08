package com.tscript.compiler.impl.generation.writers;

import com.tscript.compiler.impl.generation.compiled.instruction.*;

public interface InstructionWriter {

    void writeBinaryOperation(BinaryOperation inst);

    void writeBranchIfFalse(BranchIfFalse inst);

    void writeBranchIfTrue(BranchIfTrue inst);

    void writeBranchItr(BranchItr inst);

    void writeCallInplace(CallInplace inst);

    void writeCallMapped(CallMapped inst);

    void writeCallSuper(CallSuper inst);

    void writeContainerRead(ContainerRead inst);

    void writeContainerWrite(ContainerWrite inst);

    void writeDup(Dup inst);

    void writeEnterTry(EnterTry inst);

    void writeEquals(Equals inst);

    void writeGetItr(GetItr inst);

    void writeGetType(GetType inst);

    void writeGoto(Goto inst);

    void writeImport(Import inst);

    void writeItrNext(ItrNext inst);

    void writeLeaveTry(LeaveTry inst);

    void writeLoadAbstract(LoadAbstract inst);

    void writeLoadConst(LoadConst inst);

    void writeLoadExternal(LoadExternal inst);

    void writeStoreExternal(StoreExternal inst);

    void writeLoadInternal(LoadInternal inst);

    void writeStoreInternal(StoreInternal inst);

    void writeLoadGlobal(LoadGlobal inst);

    void writeStoreGlobal(StoreGlobal inst);

    void writeLoadLocal(LoadLocal inst);

    void writeStoreLocal(StoreLocal inst);

    void writeLoadSuper(LoadSuper inst);

    void writeStoreSuper(StoreSuper inst);

    void writeLoadBuiltin(LoadBuiltin inst);

    void writeLoadName(LoadName inst);

    void writeLoadNative(LoadNative inst);

    void writeLoadStatic(LoadStatic inst);

    void writeStoreStatic(StoreStatic inst);

    void writeLoadType(LoadType inst);

    void writeBuildType(BuildType inst);

    void writeLoadVirtual(LoadVirtual inst);

    void writeMakeArray(MakeArray inst);

    void writeMakeDict(MakeDict inst);

    void writeMakeRange(MakeRange inst);

    void writeNeg(Neg inst);

    void writeNewLine(NewLine inst);

    void writeNot(Not inst);

    void writeNotEquals(NotEquals inst);

    void writePop(Pop inst);

    void writePos(Pos inst);

    void writePushNull(PushNull inst);

    void writePushBool(PushBool inst);

    void writePushInt(PushInt inst);

    void writePushThis(PushThis inst);

    void writeReturn(Return inst);

    void writeSetOwner(SetOwner inst);

    void writeThrow(Throw inst);

    void writeToInplaceArg(ToInplaceArg inst);

    void writeToMapArg(ToMapArg inst);

    void writeUse(Use inst);
}
