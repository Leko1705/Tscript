package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.*;

import java.util.Objects;

public class IRVisitorDecorator<P, R> implements IRVisitor<P, R> {

    private final IRVisitor<P, R> visitor;

    public IRVisitorDecorator(IRVisitor<P, R> visitor) {
        this.visitor = Objects.requireNonNull(visitor);
    }

    @Override
    public R visitNewLine(NewLine node, P p) {
        return visitor.visitNewLine(node, p);
    }

    @Override
    public R visitInteger(Int node, P p) {
        return visitor.visitInteger(node, p);
    }

    @Override
    public R visitReal(Real node, P p) {
        return visitor.visitReal(node, p);
    }

    @Override
    public R visitBool(Bool node, P p) {
        return visitor.visitBool(node, p);
    }

    @Override
    public R visitNull(NullValue node, P p) {
        return visitor.visitNull(node, p);
    }

    @Override
    public R visitString(Str node, P p) {
        return visitor.visitString(node, p);
    }

    @Override
    public R visitArray(Array node, P p) {
        return visitor.visitArray(node, p);
    }

    @Override
    public R visitDictionary(Dict node, P p) {
        return visitor.visitDictionary(node, p);
    }

    @Override
    public R visitRange(Range node, P p) {
        return visitor.visitRange(node, p);
    }

    @Override
    public R visitType(Type node, P p) {
        return visitor.visitType(node, p);
    }

    @Override
    public R visitGetType(GetType node, P p) {
        return visitor.visitGetType(node, p);
    }

    @Override
    public R visitThis(This node, P p) {
        return visitor.visitThis(node, p);
    }

    @Override
    public R visitVirtual(Virtual node, P p) {
        return visitor.visitVirtual(node, p);
    }

    @Override
    public R visitNative(Native node, P p) {
        return visitor.visitNative(node, p);
    }

    @Override
    public R visitBuiltin(Builtin node, P p) {
        return visitor.visitBuiltin(node, p);
    }

    @Override
    public R visitToInpArg(ToInpArg node, P p) {
        return visitor.visitToInpArg(node, p);
    }

    @Override
    public R visitToMapArg(ToMapArg node, P p) {
        return visitor.visitToMapArg(node, p);
    }

    @Override
    public R visitCallInp(CallInp node, P p) {
        return visitor.visitCallInp(node, p);
    }

    @Override
    public R visitPushFrame(PushFrame node, P p) {
        return visitor.visitPushFrame(node, p);
    }

    @Override
    public R visitPopFrame(PopFrame node, P p) {
        return visitor.visitPopFrame(node, p);
    }

    @Override
    public R visitExprStmt(ExprStmt node, P p) {
        return visitor.visitExprStmt(node, p);
    }

    @Override
    public R visitWrapTObj(WrapTObj node, P p) {
        return visitor.visitWrapTObj(node, p);
    }

    @Override
    public R visitUnwrapTObj(UnwrapTObj node, P p) {
        return visitor.visitUnwrapTObj(node, p);
    }

    @Override
    public R visitEquals(Equals node, P p) {
        return visitor.visitEquals(node, p);
    }

    @Override
    public R visitBinaryOp(BinaryOp node, P p) {
        return visitor.visitBinaryOp(node, p);
    }

    @Override
    public R visitNeg(Neg node, P p) {
        return visitor.visitNeg(node, p);
    }

    @Override
    public R visitNot(Not node, P p) {
        return visitor.visitNot(node, p);
    }

    @Override
    public R visitReturn(Return node, P p) {
        return visitor.visitReturn(node, p);
    }

    @Override
    public R visitGuard(Guard node, P p) {
        return visitor.visitGuard(node, p);
    }

    @Override
    public R visitGoto(Goto node, P p) {
        return visitor.visitGoto(node, p);
    }

    @Override
    public R visitBranch(Branch node, P p) {
        return visitor.visitBranch(node, p);
    }

    @Override
    public R visitItrBranch(ItrBranch node, P p) {
        return visitor.visitItrBranch(node, p);
    }

    @Override
    public R visitItr(Itr node, P p) {
        return visitor.visitItr(node, p);
    }

    @Override
    public R visitItrNext(ItrNext node, P p) {
        return visitor.visitItrNext(node, p);
    }

    @Override
    public R visitItrHasNext(ItrHasNext node, P p) {
        return visitor.visitItrHasNext(node, p);
    }

    @Override
    public R visitLoadTemp(LoadTemp node, P p) {
        return visitor.visitLoadTemp(node, p);
    }

    @Override
    public R visitStoreTemp(StoreTemp node, P p) {
        return visitor.visitStoreTemp(node, p);
    }

    @Override
    public R visitLoadLocal(LoadLocal node, P p) {
        return visitor.visitLoadLocal(node, p);
    }

    @Override
    public R visitStoreLocal(StoreLocal node, P p) {
        return visitor.visitStoreLocal(node, p);
    }

    @Override
    public R visitLoadGlobal(LoadGlobal node, P p) {
        return visitor.visitLoadGlobal(node, p);
    }

    @Override
    public R visitStoreGlobal(StoreGlobal node, P p) {
        return visitor.visitStoreGlobal(node, p);
    }

    @Override
    public R visitLoadInternal(LoadInternal node, P p) {
        return visitor.visitLoadInternal(node, p);
    }

    @Override
    public R visitStoreInternal(StoreInternal node, P p) {
        return visitor.visitStoreInternal(node, p);
    }

    @Override
    public R visitLoadExternal(LoadExternal node, P p) {
        return visitor.visitLoadExternal(node, p);
    }

    @Override
    public R visitStoreExternal(StoreExternal node, P p) {
        return visitor.visitStoreExternal(node, p);
    }

    @Override
    public R visitLoadStatic(LoadStatic node, P p) {
        return visitor.visitLoadStatic(node, p);
    }

    @Override
    public R visitStoreStatic(StoreStatic node, P p) {
        return visitor.visitStoreStatic(node, p);
    }

    @Override
    public R visitLoadSuper(LoadSuper node, P p) {
        return visitor.visitLoadSuper(node, p);
    }

    @Override
    public R visitStoreSuper(StoreSuper node, P p) {
        return visitor.visitStoreSuper(node, p);
    }

    @Override
    public R visitLoadAbstract(LoadAbstract node, P p) {
        return visitor.visitLoadAbstract(node, p);
    }

    @Override
    public R visitLoadName(LoadName node, P p) {
        return visitor.visitLoadName(node, p);
    }

    @Override
    public R visitContainerRead(ContainerRead node, P p) {
        return visitor.visitContainerRead(node, p);
    }

    @Override
    public R visitContainerWrite(ContainerWrite node, P p) {
        return visitor.visitContainerWrite(node, p);
    }

    @Override
    public R visitSetOwner(SetOwner node, P p) {
        return visitor.visitSetOwner(node, p);
    }

    @Override
    public R visitExtend(Extend node, P p) {
        return visitor.visitExtend(node, p);
    }

    @Override
    public R visitImportModule(ImportModule node, P p) {
        return visitor.visitImportModule(node, p);
    }

    @Override
    public R visitUse(Use node, P p) {
        return visitor.visitUse(node, p);
    }

    @Override
    public R visitUseMembers(UseMembers node, P p) {
        return visitor.visitUseMembers(node, p);
    }
}
