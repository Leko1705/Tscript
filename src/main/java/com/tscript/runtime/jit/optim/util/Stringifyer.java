package com.tscript.runtime.jit.optim.util;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.jit.graph.expr.*;

import java.util.StringJoiner;

public class Stringifyer implements ExpressionVisitor<Void, String> {

    public static String toString(Expression expr) {
        return expr.accept(new Stringifyer(), null);
    }
    
    @Override
    public String visitReturn(Return expr, Void unused) {
        return expr.expression.accept(this, unused);
    }

    @Override
    public String visitPushFrame(PushFrame expr, Void unused) {
        return null;
    }

    @Override
    public String visitPushSuperFrame(PushSuperFrame expr, Void unused) {
        return null;
    }

    @Override
    public String visitPopFrame(PopFrame expr, Void unused) {
        return null;
    }

    @Override
    public String visitLoadTemp(LoadTemp expr, Void unused) {
        return null;
    }

    @Override
    public String visitStoreTemp(StoreTemp expr, Void unused) {
        return null;
    }

    @Override
    public String visitPop(Pop expr, Void unused) {
        return null;
    }

    @Override
    public String visitNull(Null expr, Void unused) {
        return "null";
    }

    @Override
    public String visitInt(Int expr, Void unused) {
        return Integer.toString(expr.value);
    }

    @Override
    public String visitString(Str expr, Void unused) {
        return expr.value;
    }

    @Override
    public String visitBool(Bool expr, Void unused) {
        return Boolean.toString(expr.value);
    }

    @Override
    public String visitReal(Real expr, Void unused) {
        return Double.toString(expr.value);
    }

    @Override
    public String visitRange(Range expr, Void unused) {
        String left = expr.from.accept(this, unused);
        if (left == null) return null;
        String right = expr.to.accept(this, unused);
        if (right == null) return null;
        return left + ":" + right;
    }

    @Override
    public String visitArray(Array expr, Void unused) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Expression content : expr.contents()){
            String s = content.accept(this, unused);
            if (s == null) return null;
            joiner.add(s);
        }
        return joiner.toString();
    }

    @Override
    public String visitDict(Dict expr, Void unused) {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (int i = 0; i < expr.keys.size(); i++){
            String key = expr.keys.get(i).accept(this, unused);
            if (key == null) return null;
            String value = expr.values.get(i).accept(this, unused);
            if (value == null) return null;
            joiner.add(key + ": " + value);
        }
        return joiner.toString();
    }

    @Override
    public String visitThis(This expr, Void unused) {
        return null;
    }

    @Override
    public String visitVirtual(Virtual expr, Void unused) {
        return null;
    }

    @Override
    public String visitNative(Native expr, Void unused) {
        return "Function<" + expr.name + ">";
    }

    @Override
    public String visitBuiltin(Builtin expr, Void unused) {
        return Builtins.load(expr.index()).getDisplayName();
    }

    @Override
    public String visitAbstract(Abstract expr, Void unused) {
        return null;
    }

    @Override
    public String visitCallInp(CallInp expr, Void unused) {
        return null;
    }

    @Override
    public String visitCallMap(CallMap expr, Void unused) {
        return null;
    }

    @Override
    public String visitToInpArg(ToInpArg expr, Void unused) {
        return null;
    }

    @Override
    public String visitToMapArg(ToMapArg expr, Void unused) {
        return null;
    }

    @Override
    public String visitType(Type expr, Void unused) {
        return null;
    }

    @Override
    public String visitGetType(GetType expr, Void unused) {
        return null;
    }

    @Override
    public String visitMethod(Method expr, Void unused) {
        return expr.underlying.accept(this, unused);
    }

    @Override
    public String visitBinaryOp(BinaryOp expr, Void unused) {
        return null;
    }

    @Override
    public String visitNot(Not expr, Void unused) {
        return null;
    }

    @Override
    public String visitNeg(Neg expr, Void unused) {
        if (expr.expression instanceof Int i){
            return Integer.toString(-i.value);
        }
        else if (expr.expression instanceof Real r){
            return Double.toString(-r.value);
        }
        return null;
    }

    @Override
    public String visitEquality(Equality expr, Void unused) {
        return null;
    }

    @Override
    public String visitItr(Itr expr, Void unused) {
        return null;
    }

    @Override
    public String visitItrNext(ItrNext expr, Void unused) {
        return null;
    }

    @Override
    public String visitNewLine(NewLine expr, Void unused) {
        return null;
    }

    @Override
    public String visitLoadGlobal(LoadGlobal expr, Void unused) {
        return null;
    }

    @Override
    public String visitStoreGlobal(StoreGlobal expr, Void unused) {
        return null;
    }

    @Override
    public String visitLoadLocal(LoadLocal expr, Void unused) {
        return null;
    }

    @Override
    public String visitStoreLocal(StoreLocal expr, Void unused) {
        return null;
    }

    @Override
    public String visitLoadExternal(LoadExternal expr, Void unused) {
        return null;
    }

    @Override
    public String visitStoreExternal(StoreExternal expr, Void unused) {
        return null;
    }

    @Override
    public String visitLoadInternal(LoadInternal expr, Void unused) {
        return null;
    }

    @Override
    public String visitStoreInternal(StoreInternal expr, Void unused) {
        return null;
    }

    @Override
    public String visitLoadSuper(LoadSuper expr, Void unused) {
        return null;
    }

    @Override
    public String visitStoreSuper(StoreSuper expr, Void unused) {
        return null;
    }

    @Override
    public String visitLoadStatic(LoadStatic expr, Void unused) {
        return null;
    }

    @Override
    public String visitStoreStatic(StoreStatic expr, Void unused) {
        return null;
    }

    @Override
    public String visitContainerRead(ContainerRead expr, Void unused) {
        return null;
    }

    @Override
    public String visitContainerWrite(ContainerWrite expr, Void unused) {
        return expr.value.accept(this, unused);
    }

    @Override
    public String visitLoadName(LoadName expr, Void unused) {
        return null;
    }

    @Override
    public String visitImportModule(ImportModule expr, Void unused) {
        return null;
    }

    @Override
    public String visitUse(Use expr, Void unused) {
        return null;
    }

    @Override
    public String visitUseMembers(UseMembers expr, Void unused) {
        return null;
    }

    @Override
    public String visitExtended(Extended expr, Void unused) {
        return null;
    }

    @Override
    public String visitBoolGuard(BoolGuard expr, Void unused) {
        return expr.expression.accept(this, unused);
    }

    @Override
    public String visitTypeGuard(TypeGuard expr, Void unused) {
        return expr.expression.accept(this, unused);
    }

    @Override
    public String visitItrNextGuard(ItrNextGuard expr, Void unused) {
        return null;
    }
}
