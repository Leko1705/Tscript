package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.*;

import java.util.List;

public class IRScanner<P, R> implements IRVisitor<P, R> {

    public R selectResult(R r1, R r2){
        return r1;
    }


    public R scan(IRNode node, P p) {
        return (node != null) ? node.accept(this, p) : null;
    }

    public R scanSelective(IRNode node, P p, R r) {
        return selectResult(r, scan(node, p));
    }

    public R scanSelective(List<? extends IRNode> nodes, P p, R r) {
        return selectResult(r, scan(nodes, p));
    }

    public R scan(List<? extends IRNode> nodes, P p) {
        R r = null;
        for (IRNode node : nodes) {
            r = scanSelective(node, p, r);
        }
        return r;
    }

    @Override
    public R visitNewLine(NewLine node, P p) {
        return null;
    }

    @Override
    public R visitInteger(Int node, P p) {
        return null;
    }

    @Override
    public R visitReal(Real node, P p) {
        return null;
    }

    @Override
    public R visitBool(Bool node, P p) {
        return null;
    }

    @Override
    public R visitNull(NullValue node, P p) {
        return null;
    }

    @Override
    public R visitString(Str node, P p) {
        return null;
    }

    @Override
    public R visitArray(Array node, P p) {
        return scan(node.content, p);
    }

    @Override
    public R visitDictionary(Dict node, P p) {
        R r = scan(node.keys, p);
        r = selectResult(r, scan(node.values, p));
        return r;
    }

    @Override
    public R visitRange(Range node, P p) {
        R r = scan(node.from, p);
        r = scanSelective(node.to, p, r);
        return r;
    }

    @Override
    public R visitType(Type node, P p) {
        return null;
    }

    @Override
    public R visitGetType(GetType node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitThis(This node, P p) {
        return null;
    }

    @Override
    public R visitVirtual(Virtual node, P p) {
        return null;
    }

    @Override
    public R visitNative(Native node, P p) {
        return null;
    }

    @Override
    public R visitBuiltin(Builtin node, P p) {
        return null;
    }

    @Override
    public R visitToInpArg(ToInpArg node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitToMapArg(ToMapArg node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitCallInp(CallInp node, P p) {
        R r = scan(node.called, p);
        r = scanSelective(node.args, p, r);
        return r;
    }

    @Override
    public R visitPushFrame(PushFrame node, P p) {
        return scan(node.called, p);
    }

    @Override
    public R visitPopFrame(PopFrame node, P p) {
        return null;
    }

    @Override
    public R visitExprStmt(ExprStmt node, P p) {
        return scan(node.expr, p);
    }

    @Override
    public R visitWrapTObj(WrapTObj node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitUnwrapTObj(UnwrapTObj node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitEquals(Equals node, P p) {
        R r = scan(node.lhs, p);
        r = scanSelective(node.rhs, p, r);
        return r;
    }

    @Override
    public R visitBinaryOp(BinaryOp node, P p) {
        R r = scan(node.lhs, p);
        r = scanSelective(node.rhs, p, r);
        return r;
    }

    @Override
    public R visitNeg(Neg node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitNot(Not node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitReturn(Return node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitGuard(Guard node, P p) {
        return scan(node.condition, p);
    }

    @Override
    public R visitGoto(Goto node, P p) {
        return null;
    }

    @Override
    public R visitBranch(Branch node, P p) {
        return scan(node.condition, p);
    }

    @Override
    public R visitItrBranch(ItrBranch node, P p) {
        return null;
    }

    @Override
    public R visitItr(Itr node, P p) {
        return scan(node.iterable, p);
    }

    @Override
    public R visitItrNext(ItrNext node, P p) {
        return null;
    }

    @Override
    public R visitItrHasNext(ItrHasNext node, P p) {
        return null;
    }

    @Override
    public R visitLoadTemp(LoadTemp node, P p) {
        return null;
    }

    @Override
    public R visitStoreTemp(StoreTemp node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitLoadLocal(LoadLocal node, P p) {
        return null;
    }

    @Override
    public R visitStoreLocal(StoreLocal node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitLoadGlobal(LoadGlobal node, P p) {
        return null;
    }

    @Override
    public R visitStoreGlobal(StoreGlobal node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitLoadInternal(LoadInternal node, P p) {
        return null;
    }

    @Override
    public R visitStoreInternal(StoreInternal node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitLoadExternal(LoadExternal node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitStoreExternal(StoreExternal node, P p) {
        R r = scan(node.owner, p);
        r = scanSelective(node.value, p, r);
        return r;
    }

    @Override
    public R visitLoadStatic(LoadStatic node, P p) {
        return null;
    }

    @Override
    public R visitStoreStatic(StoreStatic node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitLoadSuper(LoadSuper node, P p) {
        return null;
    }

    @Override
    public R visitStoreSuper(StoreSuper node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitLoadAbstract(LoadAbstract node, P p) {
        return null;
    }

    @Override
    public R visitLoadName(LoadName node, P p) {
        return null;
    }

    @Override
    public R visitContainerRead(ContainerRead node, P p) {
        R r = scan(node.container, p);
        r = scanSelective(node.key, p, r);
        return r;
    }

    @Override
    public R visitContainerWrite(ContainerWrite node, P p) {
        R r = scan(node.container, p);
        r = scanSelective(node.key, p, r);
        r = scanSelective(node.value, p, r);
        return r;
    }

    @Override
    public R visitSetOwner(SetOwner node, P p) {
        R r = scan(node.owner, p);
        r = scanSelective(node.member, p, r);
        return r;
    }

    @Override
    public R visitExtend(Extend node, P p) {
        R r = scan(node.owner, p);
        r = scanSelective(node.value, p, r);
        return r;
    }

    @Override
    public R visitImportModule(ImportModule node, P p) {
        return null;
    }

    @Override
    public R visitUse(Use node, P p) {
        return scan(node.value, p);
    }

    @Override
    public R visitUseMembers(UseMembers node, P p) {
        return scan(node.used, p);
    }
}
