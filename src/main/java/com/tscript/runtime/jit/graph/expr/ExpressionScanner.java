package com.tscript.runtime.jit.graph.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpressionScanner<P, R> implements ExpressionVisitor<P, R> {

    public R scan(Expression exp, P p){
        return exp != null ? exp.accept(this, p) : null;
    }

    public R scan(List<Expression> exps, P p){
        R r = null;
        for (Expression e : exps){
            r = scan(e, p);
        }
        return r;
    }

    @Override
    public R visitWrapper(Wrapper wrapper, P p) {
        return scan(wrapper.expression, p);
    }

    @Override
    public R visitPushFrame(PushFrame expr, P p) {
        return expr.accept(this, p);
    }

    @Override
    public R visitPushSuperFrame(PushSuperFrame expr, P p) {
        return null;
    }

    @Override
    public R visitPopFrame(PopFrame expr, P p) {
        return null;
    }

    @Override
    public R visitLoadTemp(LoadTemp expr, P p) {
        return null;
    }

    @Override
    public R visitStoreTemp(StoreTemp expr, P p) {
        return expr.what.accept(this, p);
    }

    @Override
    public R visitPop(Pop expr, P p) {
        return expr.expression.accept(this, p);
    }

    @Override
    public R visitNull(Null expr, P p) {
        return null;
    }

    @Override
    public R visitInt(Int expr, P p) {
        return null;
    }

    @Override
    public R visitString(Str expr, P p) {
        return null;
    }

    @Override
    public R visitBool(Bool expr, P p) {
        return null;
    }

    @Override
    public R visitReal(Real expr, P p) {
        return null;
    }

    @Override
    public R visitRange(Range expr, P p) {
        scan(expr.from, p);
        return scan(expr.to, p);
    }

    @Override
    public R visitArray(Array expr, P p) {
        return scan(expr.contents(), p);
    }

    @Override
    public R visitDict(Dict expr, P p) {
        Iterator<Expression> keyItr = new ArrayList<>(expr.keys).iterator();
        Iterator<Expression> valueItr = new ArrayList<>(expr.values).iterator();

        while (keyItr.hasNext() && valueItr.hasNext()) {
            keyItr.next().accept(this, p);
            valueItr.next().accept(this, p);
        }

        return null;
    }

    @Override
    public R visitThis(This expr, P p) {
        return null;
    }

    @Override
    public R visitVirtual(Virtual expr, P p) {
        return null;
    }

    @Override
    public R visitNative(Native expr, P p) {
        return null;
    }

    @Override
    public R visitBuiltin(Builtin expr, P p) {
        return null;
    }

    @Override
    public R visitAbstract(Abstract expr, P p) {
        return null;
    }

    @Override
    public R visitCallInp(CallInp expr, P p) {
        scan(expr.called, p);
        return scan(expr.args, p);
    }

    @Override
    public R visitCallMap(CallMap expr, P p) {
        scan(expr.called, p);
        return scan(expr.args, p);
    }

    @Override
    public R visitToInpArg(ToInpArg expr, P p) {
        return scan(expr.arg, p);
    }

    @Override
    public R visitToMapArg(ToMapArg expr, P p) {
        return scan(expr.arg, p);
    }

    @Override
    public R visitType(Type expr, P p) {
        return null;
    }

    @Override
    public R visitGetType(GetType expr, P p) {
        return scan(expr.expression, p);
    }

    @Override
    public R visitMethod(Method expr, P p) {
        expr.underlying.accept(this, p);
        return expr.owner.accept(this, p);
    }

    @Override
    public R visitBinaryOp(BinaryOp expr, P p) {
        scan(expr.lhs, p);
        return scan(expr.rhs, p);
    }

    @Override
    public R visitNot(Not expr, P p) {
        return scan(expr.expression, p);
    }

    @Override
    public R visitNeg(Neg expr, P p) {
        return scan(expr.expression, p);
    }

    @Override
    public R visitEquality(Equality expr, P p) {
        scan(expr.lhs, p);
        return scan(expr.rhs, p);
    }

    @Override
    public R visitItr(Itr expr, P p) {
        return scan(expr.iterable, p);
    }

    @Override
    public R visitItrNext(ItrNext expr, P p) {
        return null;
    }

    @Override
    public R visitNewLine(NewLine expr, P p) {
        return null;
    }

    @Override
    public R visitLoadGlobal(LoadGlobal expr, P p) {
        return null;
    }

    @Override
    public R visitStoreGlobal(StoreGlobal expr, P p) {
        return scan(expr.what, p);
    }

    @Override
    public R visitLoadLocal(LoadLocal expr, P p) {
        return null;
    }

    @Override
    public R visitStoreLocal(StoreLocal expr, P p) {
        return scan(expr.what, p);
    }

    @Override
    public R visitLoadExternal(LoadExternal expr, P p) {
        return null;
    }

    @Override
    public R visitStoreExternal(StoreExternal expr, P p) {
        return scan(expr.what, p);
    }

    @Override
    public R visitLoadInternal(LoadInternal expr, P p) {
        return null;
    }

    @Override
    public R visitStoreInternal(StoreInternal expr, P p) {
        return scan(expr.what, p);
    }

    @Override
    public R visitLoadSuper(LoadSuper expr, P p) {
        return null;
    }

    @Override
    public R visitStoreSuper(StoreSuper expr, P p) {
        return scan(expr.what, p);
    }

    @Override
    public R visitLoadStatic(LoadStatic expr, P p) {
        return null;
    }

    @Override
    public R visitStoreStatic(StoreStatic expr, P p) {
        return scan(expr.what, p);
    }

    @Override
    public R visitContainerRead(ContainerRead expr, P p) {
        scan(expr.container, p);
        return scan(expr.key, p);
    }

    @Override
    public R visitContainerWrite(ContainerWrite expr, P p) {
        scan(expr.container, p);
        scan(expr.key, p);
        return scan(expr.value, p);
    }

    @Override
    public R visitLoadName(LoadName expr, P p) {
        return null;
    }

    @Override
    public R visitImportModule(ImportModule expr, P p) {
        return null;
    }

    @Override
    public R visitUse(Use expr, P p) {
        return expr.used.accept(this, p);
    }

    @Override
    public R visitUseMembers(UseMembers expr, P p) {
        return expr.used.accept(this, p);
    }

    @Override
    public R visitExtended(Extended expr, P p) {
        scan(expr.owner, p);
        return scan(expr.content, p);
    }

    @Override
    public R visitBoolGuard(BoolGuard expr, P p) {
        return scan(expr.expression, p);
    }

    @Override
    public R visitTypeGuard(TypeGuard expr, P p) {
        return scan(expr.expression, p);
    }

    @Override
    public R visitItrNextGuard(ItrNextGuard expr, P p) {
        return null;
    }
}
