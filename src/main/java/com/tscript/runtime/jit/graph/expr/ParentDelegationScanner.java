package com.tscript.runtime.jit.graph.expr;

public class ParentDelegationScanner<R>
        extends ExpressionScanner<Expression, R>
        implements ExpressionVisitor<Expression, R> {

    public ParentDelegationScanner() {
    }

    public ParentDelegationScanner(R defaultResult) {
        super(defaultResult);
    }

    @Override
    public R visitReturn(Return expr, Expression expression) {
        return super.visitReturn(expr, expr);
    }

    @Override
    public R visitPushFrame(PushFrame expr, Expression expression) {
        return super.visitPushFrame(expr, expr);
    }

    @Override
    public R visitPushSuperFrame(PushSuperFrame expr, Expression expression) {
        return super.visitPushSuperFrame(expr, expr);
    }

    @Override
    public R visitPopFrame(PopFrame expr, Expression expression) {
        return super.visitPopFrame(expr, expr);
    }

    @Override
    public R visitLoadTemp(LoadTemp expr, Expression expression) {
        return super.visitLoadTemp(expr, expr);
    }

    @Override
    public R visitStoreTemp(StoreTemp expr, Expression expression) {
        return super.visitStoreTemp(expr, expr);
    }

    @Override
    public R visitPop(Pop expr, Expression expression) {
        return super.visitPop(expr, expr);
    }

    @Override
    public R visitNull(Null expr, Expression expression) {
        return super.visitNull(expr, expr);
    }

    @Override
    public R visitInt(Int expr, Expression expression) {
        return super.visitInt(expr, expr);
    }

    @Override
    public R visitString(Str expr, Expression expression) {
        return super.visitString(expr, expr);
    }

    @Override
    public R visitBool(Bool expr, Expression expression) {
        return super.visitBool(expr, expr);
    }

    @Override
    public R visitReal(Real expr, Expression expression) {
        return super.visitReal(expr, expr);
    }

    @Override
    public R visitRange(Range expr, Expression expression) {
        return super.visitRange(expr, expr);
    }

    @Override
    public R visitArray(Array expr, Expression expression) {
        return super.visitArray(expr, expr);
    }

    @Override
    public R visitDict(Dict expr, Expression expression) {
        return super.visitDict(expr, expr);
    }

    @Override
    public R visitThis(This expr, Expression expression) {
        return super.visitThis(expr, expr);
    }

    @Override
    public R visitVirtual(Virtual expr, Expression expression) {
        return super.visitVirtual(expr, expr);
    }

    @Override
    public R visitNative(Native expr, Expression expression) {
        return super.visitNative(expr, expr);
    }

    @Override
    public R visitBuiltin(Builtin expr, Expression expression) {
        return super.visitBuiltin(expr, expr);
    }

    @Override
    public R visitAbstract(Abstract expr, Expression expression) {
        return super.visitAbstract(expr, expr);
    }

    @Override
    public R visitCallInp(CallInp expr, Expression expression) {
        return super.visitCallInp(expr, expr);
    }

    @Override
    public R visitCallMap(CallMap expr, Expression expression) {
        return super.visitCallMap(expr, expr);
    }

    @Override
    public R visitToInpArg(ToInpArg expr, Expression expression) {
        return super.visitToInpArg(expr, expr);
    }

    @Override
    public R visitToMapArg(ToMapArg expr, Expression expression) {
        return super.visitToMapArg(expr, expr);
    }

    @Override
    public R visitType(Type expr, Expression expression) {
        return super.visitType(expr, expr);
    }

    @Override
    public R visitGetType(GetType expr, Expression expression) {
        return super.visitGetType(expr, expr);
    }

    @Override
    public R visitMethod(Method expr, Expression expression) {
        return super.visitMethod(expr, expr);
    }

    @Override
    public R visitBinaryOp(BinaryOp expr, Expression expression) {
        return super.visitBinaryOp(expr, expr);
    }

    @Override
    public R visitNot(Not expr, Expression expression) {
        return super.visitNot(expr, expr);
    }

    @Override
    public R visitNeg(Neg expr, Expression expression) {
        return super.visitNeg(expr, expr);
    }

    @Override
    public R visitEquality(Equality expr, Expression expression) {
        return super.visitEquality(expr, expr);
    }

    @Override
    public R visitItr(Itr expr, Expression expression) {
        return super.visitItr(expr, expr);
    }

    @Override
    public R visitItrNext(ItrNext expr, Expression expression) {
        return super.visitItrNext(expr, expr);
    }

    @Override
    public R visitNewLine(NewLine expr, Expression expression) {
        return super.visitNewLine(expr, expr);
    }

    @Override
    public R visitLoadGlobal(LoadGlobal expr, Expression expression) {
        return super.visitLoadGlobal(expr, expr);
    }

    @Override
    public R visitStoreGlobal(StoreGlobal expr, Expression expression) {
        return super.visitStoreGlobal(expr, expr);
    }

    @Override
    public R visitLoadLocal(LoadLocal expr, Expression expression) {
        return super.visitLoadLocal(expr, expr);
    }

    @Override
    public R visitStoreLocal(StoreLocal expr, Expression expression) {
        return super.visitStoreLocal(expr, expr);
    }

    @Override
    public R visitLoadExternal(LoadExternal expr, Expression expression) {
        return super.visitLoadExternal(expr, expr);
    }

    @Override
    public R visitStoreExternal(StoreExternal expr, Expression expression) {
        return super.visitStoreExternal(expr, expr);
    }

    @Override
    public R visitLoadInternal(LoadInternal expr, Expression expression) {
        return super.visitLoadInternal(expr, expr);
    }

    @Override
    public R visitStoreInternal(StoreInternal expr, Expression expression) {
        return super.visitStoreInternal(expr, expr);
    }

    @Override
    public R visitLoadSuper(LoadSuper expr, Expression expression) {
        return super.visitLoadSuper(expr, expr);
    }

    @Override
    public R visitStoreSuper(StoreSuper expr, Expression expression) {
        return super.visitStoreSuper(expr, expr);
    }

    @Override
    public R visitLoadStatic(LoadStatic expr, Expression expression) {
        return super.visitLoadStatic(expr, expr);
    }

    @Override
    public R visitStoreStatic(StoreStatic expr, Expression expression) {
        return super.visitStoreStatic(expr, expr);
    }

    @Override
    public R visitContainerRead(ContainerRead expr, Expression expression) {
        return super.visitContainerRead(expr, expr);
    }

    @Override
    public R visitContainerWrite(ContainerWrite expr, Expression expression) {
        return super.visitContainerWrite(expr, expr);
    }

    @Override
    public R visitLoadName(LoadName expr, Expression expression) {
        return super.visitLoadName(expr, expr);
    }

    @Override
    public R visitImportModule(ImportModule expr, Expression expression) {
        return super.visitImportModule(expr, expr);
    }

    @Override
    public R visitUse(Use expr, Expression expression) {
        return super.visitUse(expr, expr);
    }

    @Override
    public R visitUseMembers(UseMembers expr, Expression expression) {
        return super.visitUseMembers(expr, expr);
    }

    @Override
    public R visitExtended(Extended expr, Expression expression) {
        return super.visitExtended(expr, expr);
    }

    @Override
    public R visitBoolGuard(BoolGuard expr, Expression expression) {
        return super.visitBoolGuard(expr, expr);
    }

    @Override
    public R visitTypeGuard(TypeGuard expr, Expression expression) {
        return super.visitTypeGuard(expr, expr);
    }

    @Override
    public R visitItrNextGuard(ItrNextGuard expr, Expression expression) {
        return super.visitItrNextGuard(expr, expr);
    }
}
