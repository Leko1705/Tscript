package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.*;

public interface IRVisitor<P, R> {

    R visitNewLine(NewLine node, P p);

    R visitInteger(Int node, P p);

    R visitReal(Real node, P p);

    R visitBool(Bool node, P p);

    R visitNull(NullValue node, P p);

    R visitString(Str node, P p);

    R visitArray(Array node, P p);

    R visitDictionary(Dict node, P p);

    R visitRange(Range node, P p);

    R visitType(Type node, P p);

    R visitGetType(GetType node, P p);

    R visitThis(This node, P p);

    R visitVirtual(Virtual node, P p);

    R visitNative(Native node, P p);

    R visitBuiltin(Builtin node, P p);

    R visitToInpArg(ToInpArg node, P p);

    R visitToMapArg(ToMapArg node, P p);

    R visitCallInp(CallInp node, P p);

    R visitPushFrame(PushFrame node, P p);

    R visitPopFrame(PopFrame node, P p);

    R visitExprStmt(ExprStmt node, P p);

    R visitWrapTObj(WrapTObj node, P p);

    R visitUnpackObj(UnpackObj node, P p);

    R visitEquals(Equals node, P p);

    R visitBinaryOp(BinaryOp node, P p);

    R visitNeg(Neg node, P p);

    R visitNot(Not node, P p);

    R visitReturn(Return node, P p);

    R visitGuard(Guard node, P p);

    R visitGoto(Goto node, P p);

    R visitBranch(Branch node, P p);

    R visitItrBranch(ItrBranch node, P p);

    R visitItr(Itr node, P p);

    R visitItrNext(ItrNext node, P p);

    R visitItrHasNext(ItrHasNext node, P p);

    R visitLoadTemp(LoadTemp node, P p);

    R visitStoreTemp(StoreTemp node, P p);

    R visitLoadLocal(LoadLocal node, P p);

    R visitStoreLocal(StoreLocal node, P p);

    R visitLoadGlobal(LoadGlobal node, P p);

    R visitStoreGlobal(StoreGlobal node, P p);

    R visitLoadInternal(LoadInternal node, P p);

    R visitStoreInternal(StoreInternal node, P p);

    R visitLoadExternal(LoadExternal node, P p);

    R visitStoreExternal(StoreExternal node, P p);

    R visitLoadStatic(LoadStatic node, P p);

    R visitStoreStatic(StoreStatic node, P p);

    R visitLoadSuper(LoadSuper node, P p);

    R visitStoreSuper(StoreSuper node, P p);

    R visitLoadAbstract(LoadAbstract node, P p);

    R visitLoadName(LoadName node, P p);

    R visitContainerRead(ContainerRead node, P p);

    R visitContainerWrite(ContainerWrite node, P p);

    R visitSetOwner(SetOwner node, P p);

    R visitExtend(Extend node, P p);

    R visitImportModule(ImportModule node, P p);

    R visitUse(Use node, P p);

    R visitUseMembers(UseMembers node, P p);
}
