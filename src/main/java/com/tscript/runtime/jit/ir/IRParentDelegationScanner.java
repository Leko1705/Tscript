package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.*;

public class IRParentDelegationScanner<R> extends IRScanner<IRNode, R> {
    
    @Override
    public R visitNewLine(NewLine node, IRNode parent) {
        return super.visitNewLine(node, node);
    }

    @Override
    public R visitInteger(Int node, IRNode parent) {
        return super.visitInteger(node, node);
    }

    @Override
    public R visitReal(Real node, IRNode parent) {
        return super.visitReal(node, node);
    }

    @Override
    public R visitBool(Bool node, IRNode parent) {
        return super.visitBool(node, node);
    }

    @Override
    public R visitNull(NullValue node, IRNode parent) {
        return super.visitNull(node, node);
    }

    @Override
    public R visitString(Str node, IRNode parent) {
        return super.visitString(node, node);
    }

    @Override
    public R visitArray(Array node, IRNode parent) {
        return super.visitArray(node, node);
    }

    @Override
    public R visitDictionary(Dict node, IRNode parent) {
        return super.visitDictionary(node, node);
    }

    @Override
    public R visitRange(Range node, IRNode parent) {
        return super.visitRange(node, node);
    }

    @Override
    public R visitType(Type node, IRNode parent) {
        return super.visitType(node, node);
    }

    @Override
    public R visitGetType(GetType node, IRNode parent) {
        return super.visitGetType(node, node);
    }

    @Override
    public R visitThis(This node, IRNode parent) {
        return super.visitThis(node, node);
    }

    @Override
    public R visitVirtual(Virtual node, IRNode parent) {
        return super.visitVirtual(node, node);
    }

    @Override
    public R visitNative(Native node, IRNode parent) {
        return super.visitNative(node, node);
    }

    @Override
    public R visitBuiltin(Builtin node, IRNode parent) {
        return super.visitBuiltin(node, node);
    }

    @Override
    public R visitToInpArg(ToInpArg node, IRNode parent) {
        return super.visitToInpArg(node, node);
    }

    @Override
    public R visitToMapArg(ToMapArg node, IRNode parent) {
        return super.visitToMapArg(node, node);
    }

    @Override
    public R visitCallInp(CallInp node, IRNode parent) {
        return super.visitCallInp(node, node);
    }

    @Override
    public R visitPushFrame(PushFrame node, IRNode parent) {
        return super.visitPushFrame(node, node);
    }

    @Override
    public R visitPopFrame(PopFrame node, IRNode parent) {
        return super.visitPopFrame(node, node);
    }

    @Override
    public R visitExprStmt(ExprStmt node, IRNode parent) {
        return super.visitExprStmt(node, node);
    }

    @Override
    public R visitWrapTObj(WrapTObj node, IRNode parent) {
        return super.visitWrapTObj(node, node);
    }

    @Override
    public R visitUnpackObj(UnpackObj node, IRNode parent) {
        return super.visitUnpackObj(node, node);
    }

    @Override
    public R visitEquals(Equals node, IRNode parent) {
        return super.visitEquals(node, node);
    }

    @Override
    public R visitBinaryOp(BinaryOp node, IRNode parent) {
        return super.visitBinaryOp(node, node);
    }

    @Override
    public R visitNeg(Neg node, IRNode parent) {
        return super.visitNeg(node, node);
    }

    @Override
    public R visitNot(Not node, IRNode parent) {
        return super.visitNot(node, node);
    }

    @Override
    public R visitReturn(Return node, IRNode parent) {
        return super.visitReturn(node, node);
    }

    @Override
    public R visitGuard(Guard node, IRNode parent) {
        return super.visitGuard(node, node);
    }

    @Override
    public R visitGoto(Goto node, IRNode parent) {
        return super.visitGoto(node, node);
    }

    @Override
    public R visitBranch(Branch node, IRNode parent) {
        return super.visitBranch(node, node);
    }

    @Override
    public R visitItrBranch(ItrBranch node, IRNode parent) {
        return super.visitItrBranch(node, node);
    }

    @Override
    public R visitItr(Itr node, IRNode parent) {
        return super.visitItr(node, node);
    }

    @Override
    public R visitItrNext(ItrNext node, IRNode parent) {
        return super.visitItrNext(node, node);
    }

    @Override
    public R visitItrHasNext(ItrHasNext node, IRNode parent) {
        return super.visitItrHasNext(node, node);
    }

    @Override
    public R visitLoadTemp(LoadTemp node, IRNode parent) {
        return super.visitLoadTemp(node, node);
    }

    @Override
    public R visitStoreTemp(StoreTemp node, IRNode parent) {
        return super.visitStoreTemp(node, node);
    }

    @Override
    public R visitLoadLocal(LoadLocal node, IRNode parent) {
        return super.visitLoadLocal(node, node);
    }

    @Override
    public R visitStoreLocal(StoreLocal node, IRNode parent) {
        return super.visitStoreLocal(node, node);
    }

    @Override
    public R visitLoadGlobal(LoadGlobal node, IRNode parent) {
        return super.visitLoadGlobal(node, node);
    }

    @Override
    public R visitStoreGlobal(StoreGlobal node, IRNode parent) {
        return super.visitStoreGlobal(node, node);
    }

    @Override
    public R visitLoadInternal(LoadInternal node, IRNode parent) {
        return super.visitLoadInternal(node, node);
    }

    @Override
    public R visitStoreInternal(StoreInternal node, IRNode parent) {
        return super.visitStoreInternal(node, node);
    }

    @Override
    public R visitLoadExternal(LoadExternal node, IRNode parent) {
        return super.visitLoadExternal(node, node);
    }

    @Override
    public R visitStoreExternal(StoreExternal node, IRNode parent) {
        return super.visitStoreExternal(node, node);
    }

    @Override
    public R visitLoadStatic(LoadStatic node, IRNode parent) {
        return super.visitLoadStatic(node, node);
    }

    @Override
    public R visitStoreStatic(StoreStatic node, IRNode parent) {
        return super.visitStoreStatic(node, node);
    }

    @Override
    public R visitLoadSuper(LoadSuper node, IRNode parent) {
        return super.visitLoadSuper(node, node);
    }

    @Override
    public R visitStoreSuper(StoreSuper node, IRNode parent) {
        return super.visitStoreSuper(node, node);
    }

    @Override
    public R visitLoadAbstract(LoadAbstract node, IRNode parent) {
        return super.visitLoadAbstract(node, node);
    }

    @Override
    public R visitLoadName(LoadName node, IRNode parent) {
        return super.visitLoadName(node, node);
    }

    @Override
    public R visitContainerRead(ContainerRead node, IRNode parent) {
        return super.visitContainerRead(node, node);
    }

    @Override
    public R visitContainerWrite(ContainerWrite node, IRNode parent) {
        return super.visitContainerWrite(node, node);
    }

    @Override
    public R visitSetOwner(SetOwner node, IRNode parent) {
        return super.visitSetOwner(node, node);
    }

    @Override
    public R visitExtend(Extend node, IRNode parent) {
        return super.visitExtend(node, node);
    }

    @Override
    public R visitImportModule(ImportModule node, IRNode parent) {
        return super.visitImportModule(node, node);
    }

    @Override
    public R visitUse(Use node, IRNode parent) {
        return super.visitUse(node, node);
    }

    @Override
    public R visitUseMembers(UseMembers node, IRNode parent) {
        return super.visitUseMembers(node, node);
    }
}
