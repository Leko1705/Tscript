package com.tscript.runtime.jit.graph.expr;

public interface ExpressionVisitor<P, R> {

    R visitPushFrame(PushFrame expr, P p);

    R visitPopFrame(PopFrame expr, P p);

    R visitPop(Pop expr, P p);

    R visitNull(Null expr, P p);

    R visitInt(Int expr, P p);

    R visitString(Str expr, P p);

    R visitBool(Bool expr, P p);

    R visitReal(Real expr, P p);

    R visitRange(Range expr, P p);

    R visitArray(Array expr, P p);

    R visitDict(Dict expr, P p);

    R visitThis(This expr, P p);

    R visitVirtual(Virtual expr, P p);

    R visitNative(Native expr, P p);

    R visitBuiltin(Builtin expr, P p);

    R visitAbstract(Abstract expr, P p);

    R visitCallInp(CallInp expr, P p);

    R visitCallMap(CallMap expr, P p);

    R visitToInpArg(ToInpArg expr, P p);

    R visitToMapArg(ToMapArg expr, P p);

    R visitType(Type expr, P p);

    R visitGetType(GetType expr, P p);

    R visitMethod(Method expr, P p);

    R visitBinaryOp(BinaryOp expr, P p);

    R visitNot(Not expr, P p);

    R visitNeg(Neg expr, P p);

    R visitEquality(Equality expr, P p);

    R visitItr(Itr expr, P p);

    R visitItrNext(ItrNext expr, P p);

    R visitNewLine(NewLine expr, P p);

    R visitLoadGlobal(LoadGlobal expr, P p);

    R visitStoreGlobal(StoreGlobal expr, P p);

    R visitLoadLocal(LoadLocal expr, P p);

    R visitStoreLocal(StoreLocal expr, P p);

    R visitLoadExternal(LoadExternal expr, P p);

    R visitStoreExternal(StoreExternal expr, P p);

    R visitLoadInternal(LoadInternal expr, P p);

    R visitStoreInternal(StoreInternal expr, P p);

    R visitLoadSuper(LoadSuper expr, P p);

    R visitStoreSuper(StoreSuper expr, P p);

     R visitLoadStatic(LoadStatic expr, P p);

     R visitStoreStatic(StoreStatic expr, P p);

     R visitContainerRead(ContainerRead expr, P p);

     R visitContainerWrite(ContainerWrite expr, P p);

     R visitLoadName(LoadName expr, P p);

     R visitImportModule(ImportModule expr, P p);

     R visitBoolGuard(BoolGuard expr, P p);

     R visitTypeGuard(TypeGuard expr, P p);

     R visitItrNextGuard(ItrNextGuard expr, P p);
}
