package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.*;

public class IRVisitorImpl<P, R> implements IRVisitor<P, R> {

    public R defaultValue;

    public IRVisitorImpl() {
        this.defaultValue = null;
    }

    public IRVisitorImpl(R defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public R defaultOperation(IRNode node, P p) {
        return defaultValue;
    }
    
    @Override
    public R visitNewLine(NewLine node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitInteger(Int node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitReal(Real node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitBool(Bool node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitNull(NullValue node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitString(Str node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitArray(Array node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitDictionary(Dict node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitRange(Range node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitType(Type node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitGetType(GetType node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitThis(This node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitVirtual(Virtual node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitNative(Native node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitBuiltin(Builtin node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitToInpArg(ToInpArg node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitToMapArg(ToMapArg node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitCallInp(CallInp node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitPushFrame(PushFrame node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitPopFrame(PopFrame node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitExprStmt(ExprStmt node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitWrapTObj(WrapTObj node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitUnwrapTObj(UnwrapTObj node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitEquals(Equals node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitBinaryOp(BinaryOp node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitNeg(Neg node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitNot(Not node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitReturn(Return node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitGuard(Guard node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitGoto(Goto node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitBranch(Branch node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitItrBranch(ItrBranch node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitItr(Itr node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitItrNext(ItrNext node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitItrHasNext(ItrHasNext node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadTemp(LoadTemp node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitStoreTemp(StoreTemp node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadLocal(LoadLocal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitStoreLocal(StoreLocal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadGlobal(LoadGlobal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitStoreGlobal(StoreGlobal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadInternal(LoadInternal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitStoreInternal(StoreInternal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadExternal(LoadExternal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitStoreExternal(StoreExternal node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadStatic(LoadStatic node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitStoreStatic(StoreStatic node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadSuper(LoadSuper node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitStoreSuper(StoreSuper node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadAbstract(LoadAbstract node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitLoadName(LoadName node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitContainerRead(ContainerRead node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitContainerWrite(ContainerWrite node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitSetOwner(SetOwner node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitExtend(Extend node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitImportModule(ImportModule node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitUse(Use node, P p) {
        return defaultOperation(node, p);
    }

    @Override
    public R visitUseMembers(UseMembers node, P p) {
        return defaultOperation(node, p);
    }
}
