package com.tscript.runtime.jit.comp.analysis;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.typing.TObject;

/**
 * Infers the static type of a value node. The rules mirror the semantics of the interpreter
 * (see ALU): a precise type is only inferred if the operation is guaranteed to produce it.
 * Phis are handled by {@link Graph.TypeMap}.
 */
public class Typing {

    public static IRType infer(IRValue value, Graph.TypeMap typeMap) {
        return value.accept(new Infer(typeMap));
    }

    private static final IRType UNKNOWN = UnknownType.INSTANCE;
    private static final IRType INT = IntType.INSTANCE;
    private static final IRType REAL = RealType.INSTANCE;
    private static final IRType BOOL = BoolType.INSTANCE;
    private static final IRType STR = StrType.INSTANCE;

    private record Infer(Graph.TypeMap typeMap) implements IRValueVisitor<Void, IRType> {

        private IRType type(IRValue value) {
            return typeMap.getType(value);
        }

        private boolean isNumeric(IRType type) {
            return type == INT || type == REAL;
        }

        private IRType arithmetic(BinaryOp op) {
            IRType l = type(op.getLeft()), r = type(op.getRight());
            if (l == INT && r == INT) return INT;
            if (isNumeric(l) && isNumeric(r)) return REAL;
            return UNKNOWN;
        }

        private IRType intOnly(BinaryOp op) {
            IRType l = type(op.getLeft()), r = type(op.getRight());
            return l == INT && r == INT ? INT : UNKNOWN;
        }

        private IRType logical(BinaryOp op) {
            IRType l = type(op.getLeft()), r = type(op.getRight());
            if (l == INT && r == INT) return INT;
            if (l == BOOL && r == BOOL) return BOOL;
            return UNKNOWN;
        }

        private IRType comparison(BinaryOp op) {
            IRType l = type(op.getLeft()), r = type(op.getRight());
            return isNumeric(l) && isNumeric(r) ? BOOL : UNKNOWN;
        }

        @Override
        public IRType visitInteger(Int node, Void unused) {
            return INT;
        }

        @Override
        public IRType visitReal(Real node, Void unused) {
            return REAL;
        }

        @Override
        public IRType visitString(Str node, Void unused) {
            return STR;
        }

        @Override
        public IRType visitBool(Bool node, Void unused) {
            return BOOL;
        }

        @Override
        public IRType visitNull(NullValue node, Void unused) {
            return NullType.INSTANCE;
        }

        @Override
        public IRType visitConstant(Constant node, Void unused) {
            return IRType.from((TObject) ObjectCache.getObject(node.index), false);
        }

        @Override
        public IRType visitBuiltin(Builtin node, Void unused) {
            return IRType.from(Builtins.load(node.index), false);
        }

        @Override
        public IRType visitType(Type node, Void unused) {
            return new TypeMirror(UNKNOWN);
        }

        @Override
        public IRType visitThis(This node, Void unused) {
            return UNKNOWN;
        }

        @Override
        public IRType visitLocal(Local node, Void unused) {
            return node.type;
        }

        @Override
        public IRType visitStackValue(StackValue node, Void unused) {
            return node.type;
        }

        @Override
        public IRType visitParameter(Parameter node, Void unused) {
            return node.type;
        }

        @Override
        public IRType visitPhi(Phi node, Void unused) {
            throw new AssertionError("phis are typed by the TypeMap");
        }

        @Override
        public IRType visitPi(Pi node, Void unused) {
            return node.type;
        }

        @Override
        public IRType visitAdd(Add node, Void unused) {
            IRType l = type(node.getLeft()), r = type(node.getRight());
            if ((l == STR && IRType.isPrimitive(r)) || (r == STR && IRType.isPrimitive(l))) return STR;
            return arithmetic(node);
        }

        @Override
        public IRType visitSub(Sub node, Void unused) {
            return arithmetic(node);
        }

        @Override
        public IRType visitMul(Mul node, Void unused) {
            return arithmetic(node);
        }

        @Override
        public IRType visitDiv(Div node, Void unused) {
            IRType l = type(node.getLeft()), r = type(node.getRight());
            return isNumeric(l) && isNumeric(r) ? REAL : UNKNOWN;
        }

        @Override
        public IRType visitIDiv(IDiv node, Void unused) {
            return intOnly(node);
        }

        @Override
        public IRType visitMod(Mod node, Void unused) {
            return intOnly(node);
        }

        @Override
        public IRType visitPow(Pow node, Void unused) {
            return arithmetic(node);
        }

        @Override
        public IRType visitShl(Shl node, Void unused) {
            return intOnly(node);
        }

        @Override
        public IRType visitShr(Shr node, Void unused) {
            return intOnly(node);
        }

        @Override
        public IRType visitUshr(Ushr node, Void unused) {
            return intOnly(node);
        }

        @Override
        public IRType visitAnd(And node, Void unused) {
            return logical(node);
        }

        @Override
        public IRType visitOr(Or node, Void unused) {
            return logical(node);
        }

        @Override
        public IRType visitXor(Xor node, Void unused) {
            return logical(node);
        }

        @Override
        public IRType visitLt(Lt node, Void unused) {
            return comparison(node);
        }

        @Override
        public IRType visitLeq(Leq node, Void unused) {
            return comparison(node);
        }

        @Override
        public IRType visitEquals(Equals node, Void unused) {
            return BOOL;
        }

        @Override
        public IRType visitNot(Not node, Void unused) {
            IRType t = type(node.getValue());
            return t == INT || t == BOOL ? t : UNKNOWN;
        }

        @Override
        public IRType visitNeg(Neg node, Void unused) {
            IRType t = type(node.getValue());
            return isNumeric(t) ? t : UNKNOWN;
        }

        @Override
        public IRType visitPos(Pos node, Void unused) {
            IRType t = type(node.getValue());
            return isNumeric(t) ? t : UNKNOWN;
        }

        @Override
        public IRType visitTypeof(Typeof node, Void unused) {
            return new TypeMirror(UNKNOWN);
        }

        @Override
        public IRType visitIsType(IsType node, Void unused) {
            return BOOL;
        }

        @Override
        public IRType visitTruthy(Truthy node, Void unused) {
            return BOOL;
        }

        @Override
        public IRType visitIsSelf(IsSelf node, Void unused) {
            return BOOL;
        }

        @Override
        public IRType visitRange(Range node, Void unused) {
            return RangeType.INSTANCE;
        }

        @Override
        public IRType visitArray(Array node, Void unused) {
            return new ArrayType(UNKNOWN);
        }

        @Override
        public IRType visitDict(Dict node, Void unused) {
            return new DictType(UNKNOWN, UNKNOWN);
        }

        @Override
        public IRType visitCall(Call node, Void unused) {
            return UNKNOWN;
        }

        @Override
        public IRType visitGlobal(Global node, Void unused) {
            return UNKNOWN;
        }

        @Override
        public IRType visitGetIterator(GetIterator node, Void unused) {
            return UNKNOWN;
        }

        @Override
        public IRType visitIterHasNext(IterHasNext node, Void unused) {
            return BOOL;
        }

        @Override
        public IRType visitIterNext(IterNext node, Void unused) {
            return type(node.getValue()) == RangeIteratorType.INSTANCE ? INT : UNKNOWN;
        }

        @Override
        public IRType visitIsCallee(IsCallee node, Void unused) {
            return BOOL;
        }

        @Override
        public IRType visitFunctionOwner(FunctionOwner node, Void unused) {
            return UNKNOWN;
        }

        @Override
        public IRType visitNewRangeIterator(NewRangeIterator node, Void unused) {
            return RangeIteratorType.INSTANCE;
        }

        @Override
        public IRType visitContainerRead(ContainerRead node, Void unused) {
            return UNKNOWN;
        }
    }

}
