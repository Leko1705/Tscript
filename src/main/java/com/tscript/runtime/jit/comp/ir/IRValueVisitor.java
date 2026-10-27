package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

public interface IRValueVisitor<P, R> {

    R visitInteger(Int node, P p);
    
    R visitReal(Real node, P p);

    R visitString(Str node, P p);

    R visitBool(Bool node, P p);

    R visitNull(NullValue node, P p);

    R visitArray(Array node, P p);

    R visitDict(Dict node, P p);

    R visitRange(Range node, P p);

    R visitThis(This node, P p);

    R visitAdd(Add node, P p);

    R visitSub(Sub node, P p);

    R visitMul(Mul node, P p);

    R visitDiv(Div node, P p);

    R visitIDiv(IDiv node, P p);

    R visitMod(Mod node, P p);

    R visitPow(Pow node, P p);

    R visitShl(Shl node, P p);

    R visitShr(Shr node, P p);

    R visitUshr(Ushr node, P p);

    R visitAnd(And node, P p);

    R visitOr(Or node, P p);

    R visitXor(Xor node, P p);

    R visitNot(Not node, P p);

    R visitNeg(Neg node, P p);

    R visitPos(Pos node, P p);

    R visitType(Type node, P p);

    R visitTypeof(Typeof node, P p);

    R visitGt(Gt node, P p);

    R visitLt(Lt node, P p);

    R visitEquals(Equals node, P p);

    R visitParameter(Parameter node, P p);

    R visitBuiltin(Builtin node, P p);

    R visitToInpArg(ToInpArg node, P p);

    R visitToMapArg(ToMapArg node, P p);

    R visitCall(Call node, P p);

    R visitToString(ToString node, P p);

    R visitPhi(Phi node, P p);

    R visitLocal(Local node, P p);

    R visitGlobal(Global node, P p);

}
