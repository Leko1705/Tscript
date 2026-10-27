package com.tscript.runtime.jit.comp.utils;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.nodes.*;

import java.util.List;

public class IRValueScanner<P, R> implements IRValueVisitor<P, R> {

    public R scan(IRValue value, P p) {
        return value != null ? value.accept(this, p) : null;
    }

    public R scan(IRValue value) {
        return scan(value, null);
    }

    public R scan(Iterable<IRValue> values, P p) {
        return scanSelective(values, p, null);
    }

    public R scanSelective(Iterable<IRValue> values, P p, R r) {
        R result = r;
        for (IRValue v : values) {
            result = scanSelective(v, p, r);
        }
        return result;
    }

    public R scan(Iterable<IRValue> values) {
        return scan(values, null);
    }

    public R scanSelective(IRValue value, P p, R r) {
        return select(r, scan(value, p));
    }

    public R select(R r1, R r2) {
        return r2;
    }

    @Override
    public R visitInteger(Int node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitReal(Real node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitString(Str node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitBool(Bool node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitNull(NullValue node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitArray(Array node, P p) {
        return scan(node.getValues(), p);
    }

    @Override
    public R visitDict(Dict node, P p) {
        List<IRValue> keys = node.getKeys();
        List<IRValue> values = node.getValues();

        R r = null;
        for (int i = 0; i != keys.size(); ++i) {
            r = scanSelective(keys.get(i), p, r);
            r = scanSelective(values.get(i), p, r);
        }

        return r;
    }

    @Override
    public R visitRange(Range node, P p) {
        R r = scan(node.getFrom(), p);
        return scanSelective(node.getTo(), p, r);
    }

    @Override
    public R visitThis(This node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitAdd(Add node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitSub(Sub node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitMul(Mul node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitDiv(Div node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitIDiv(IDiv node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitMod(Mod node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitPow(Pow node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitShl(Shl node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitShr(Shr node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitUshr(Ushr node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitAnd(And node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitOr(Or node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitXor(Xor node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitNot(Not node, P p) {
        return scan(node.getValue(), p);
    }

    @Override
    public R visitNeg(Neg node, P p) {
        return scan(node.getValue(), p);
    }

    @Override
    public R visitPos(Pos node, P p) {
        return scan(node.getValue(), p);
    }

    @Override
    public R visitType(Type node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitTypeof(Typeof node, P p) {
        return scan(node.getValue(), p);
    }

    @Override
    public R visitGt(Gt node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitLt(Lt node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitEquals(Equals node, P p) {
        return visitBinary(node, p);
    }

    @Override
    public R visitParameter(Parameter node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitBuiltin(Builtin node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitToInpArg(ToInpArg node, P p) {
        return scan(node.getValue(), p);
    }

    @Override
    public R visitToMapArg(ToMapArg node, P p) {
        return scan(node.getValue(), p);
    }

    @Override
    public R visitCall(Call node, P p) {
        R r = scan(node.getCalled(), p);
        return scanSelective(node.getCalled(), p, r);
    }

    @Override
    public R visitToString(ToString node, P p) {
        return scan(node.getValue(), p);
    }

    @Override
    public R visitPhi(Phi node, P p) {
        return scan(node.getArgs(), p);
    }

    @Override
    public R visitLocal(Local node, P p) {
        return visitValue(node, p);
    }

    @Override
    public R visitGlobal(Global node, P p) {
        return visitValue(node, p);
    }

    public R visitBinary(BinaryOp node, P p) {
        R r = scan(node.getLeft(), p);
        return scanSelective(node.getRight(), p, r);
    }

    public R visitValue(IRValue node, P p) {
        return null;
    }
}
