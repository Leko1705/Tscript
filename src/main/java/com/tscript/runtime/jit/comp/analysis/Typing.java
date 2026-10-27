package com.tscript.runtime.jit.comp.analysis;

import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.nodes.Range;
import com.tscript.runtime.jit.comp.ir.nodes.Type;
import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.typing.*;

import java.util.List;

public class Typing {

    public static IRType infer(IRValue value, Graph.TypeMap typeMap) {
        Infer infer = new Infer(typeMap);
        return value.accept(infer);
    }


    private record Infer(Graph.TypeMap typeMap) implements IRValueVisitor<Void, IRType> {

        private static final UnionType ADD_TYPE = new UnionType(IntType.INSTANCE, RealType.INSTANCE, StrType.INSTANCE);
        private static final UnionType NUMERIC_TYPE = new UnionType(IntType.INSTANCE, RealType.INSTANCE);
        private static final UnionType INT_OR_BOOL = new UnionType(IntType.INSTANCE, BoolType.INSTANCE);

        @Override
        public IRType visitInteger(Int node, Void unused) {
            return IntType.INSTANCE;
        }

        @Override
        public IRType visitReal(Real node, Void unused) {
            return RealType.INSTANCE;
        }

        @Override
        public IRType visitString(Str node, Void unused) {
            return StrType.INSTANCE;
        }

        @Override
        public IRType visitBool(Bool node, Void unused) {
            return BoolType.INSTANCE;
        }

        @Override
        public IRType visitNull(NullValue node, Void unused) {
            return NullType.INSTANCE;
        }

        @Override
        public IRType visitArray(Array node, Void unused) {
            IRType parameter = unionTypes(node.getValues());
            return typeMap.put(node, new ArrayType(parameter));
        }

        @Override
        public IRType visitDict(Dict node, Void unused) {
            IRType keyParameter = unionTypes(node.getKeys());
            IRType valueParameter = unionTypes(node.getValues());
            return typeMap.put(node, new DictType(keyParameter, valueParameter));
        }

        @Override
        public IRType visitRange(Range node, Void unused) {
            typeMap.getType(node.getFrom());  // trigger sub-expr-infer
            typeMap.getType(node.getTo());  // trigger sub-expr-infer
            return RangeType.INSTANCE;
        }

        @Override
        public IRType visitThis(This node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public IRType visitAdd(Add node, Void unused) {
            if (anyOfType(node, StrType.INSTANCE)) return typeMap.put(node, StrType.INSTANCE);

            if (bothOfType(node, IntType.INSTANCE, RealType.INSTANCE)) {
                if (bothOfType(node, IntType.INSTANCE))
                    return typeMap.put(node, IntType.INSTANCE);
                return typeMap.put(node, RealType.INSTANCE);
            }

            return typeMap.put(node, ADD_TYPE);
        }

        @Override
        public IRType visitSub(Sub node, Void unused) {
            if (anyOfType(node, IntType.INSTANCE, RealType.INSTANCE)) {
                if (bothOfType(node, IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
                return typeMap.put(node, RealType.INSTANCE);
            }
            return typeMap.put(node, NUMERIC_TYPE);
        }

        @Override
        public IRType visitMul(Mul node, Void unused) {
            if (anyOfType(node, IntType.INSTANCE, RealType.INSTANCE)) {
                if (bothOfType(node, IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
                return typeMap.put(node, RealType.INSTANCE);
            }
            return typeMap.put(node, NUMERIC_TYPE);
        }

        @Override
        public IRType visitDiv(Div node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, RealType.INSTANCE);
        }

        @Override
        public IRType visitIDiv(IDiv node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, IntType.INSTANCE);
        }

        @Override
        public IRType visitMod(Mod node, Void unused) {
            if (bothOfType(node, IntType.INSTANCE))  return typeMap.put(node, IntType.INSTANCE);
            if (bothOfType(node, BoolType.INSTANCE))  return typeMap.put(node, BoolType.INSTANCE);
            return typeMap.put(node, INT_OR_BOOL);
        }

        @Override
        public IRType visitPow(Pow node, Void unused) {
            if (anyOfType(node, IntType.INSTANCE, RealType.INSTANCE)) {
                if (bothOfType(node, IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
                return typeMap.put(node, RealType.INSTANCE);
            }
            return typeMap.put(node, NUMERIC_TYPE);
        }

        @Override
        public IRType visitAnd(And node, Void unused) {
            if (bothOfType(node, IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
            if (bothOfType(node, BoolType.INSTANCE)) return typeMap.put(node, BoolType.INSTANCE);
            return typeMap.put(node, INT_OR_BOOL);
        }

        @Override
        public IRType visitShl(Shl node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, IntType.INSTANCE);
        }

        @Override
        public IRType visitShr(Shr node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, IntType.INSTANCE);
        }

        @Override
        public IRType visitUshr(Ushr node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, IntType.INSTANCE);
        }

        @Override
        public IRType visitOr(Or node, Void unused) {
            if (bothOfType(node, IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
            if (bothOfType(node, BoolType.INSTANCE)) return typeMap.put(node, BoolType.INSTANCE);
            return typeMap.put(node, INT_OR_BOOL);
        }

        @Override
        public IRType visitXor(Xor node, Void unused) {
            if (bothOfType(node, IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
            if (bothOfType(node, BoolType.INSTANCE)) return typeMap.put(node, BoolType.INSTANCE);
            return typeMap.put(node, INT_OR_BOOL);
        }

        @Override
        public IRType visitNot(Not node, Void unused) {
            if (isType(node.getValue(), IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
            if (isType(node.getValue(), BoolType.INSTANCE)) return typeMap.put(node, BoolType.INSTANCE);
            return typeMap.put(node, INT_OR_BOOL);
        }

        @Override
        public IRType visitNeg(Neg node, Void unused) {
            if (isType(node.getValue(), IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
            if (isType(node.getValue(), RealType.INSTANCE)) return typeMap.put(node, RealType.INSTANCE);
            return typeMap.put(node, NUMERIC_TYPE);
        }

        @Override
        public IRType visitPos(Pos node, Void unused) {
            if (isType(node.getValue(), IntType.INSTANCE)) return typeMap.put(node, IntType.INSTANCE);
            if (isType(node.getValue(), RealType.INSTANCE)) return typeMap.put(node, RealType.INSTANCE);
            return typeMap.put(node, NUMERIC_TYPE);
        }

        @Override
        public IRType visitType(Type node, Void unused) {
            com.tscript.runtime.typing.Type ttype =
                    (com.tscript.runtime.typing.Type) ObjectCache.getObject(node.getAddress());

            IRType typeParam = new ComplexType(ttype);

            if (ttype == Null.TYPE) typeParam = NullType.INSTANCE;
            if (ttype == TInteger.TYPE) typeParam = IntType.INSTANCE;
            if (ttype == TReal.TYPE) typeParam = RealType.INSTANCE;
            if (ttype == TBoolean.TYPE) typeParam = BoolType.INSTANCE;
            if (ttype == TString.TYPE) typeParam = StrType.INSTANCE;
            if (ttype == com.tscript.runtime.typing.Range.TYPE) typeParam = RangeType.INSTANCE;

            return new TypeMirror(typeParam);
        }

        @Override
        public IRType visitTypeof(Typeof node, Void unused) {
            IRType type = typeMap.getType(node.getValue());
            if (type instanceof TypeMirror m && m.getParameters().get(0) instanceof TypeMirror) {
                // do not allow Type<Type<Type<?>>>; only a maximum of Type<Type<?>>
                return type;
            }
            return new TypeMirror(type);
        }

        @Override
        public IRType visitGt(Gt node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, BoolType.INSTANCE);
        }

        @Override
        public IRType visitLt(Lt node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, BoolType.INSTANCE);
        }

        @Override
        public IRType visitEquals(Equals node, Void unused) {
            typeMap.getType(node.getLeft());  // trigger sub-expr-infer
            typeMap.getType(node.getRight());  // trigger sub-expr-infer
            return typeMap.put(node, BoolType.INSTANCE);
        }

        @Override
        public IRType visitParameter(Parameter node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public IRType visitBuiltin(Builtin node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public IRType visitToInpArg(ToInpArg node, Void unused) {
            return typeMap.getType(node.getValue());
        }

        @Override
        public IRType visitToMapArg(ToMapArg node, Void unused) {
            return typeMap.getType(node.getValue());
        }

        @Override
        public IRType visitCall(Call node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public IRType visitToString(ToString node, Void unused) {
            typeMap.getType(node.getValue());  // trigger sub-expr-infer
            return StrType.INSTANCE;
        }

        @Override
        public IRType visitPhi(Phi node, Void unused) {
            List<IRType> args = node.getArgs().stream().map(typeMap::getType).toList();
            return typeMap.put(node, UnionType.mixup(args));
        }

        @Override
        public IRType visitLocal(Local node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public IRType visitGlobal(Global node, Void unused) {
            return UnknownType.INSTANCE;
        }

        private IRType unionTypes(Iterable<IRValue> values) {
            IRType mixed = null;
            for (IRValue value : values) {
                if (mixed == null) {
                    mixed = typeMap.getType(value);
                }
                else {
                    mixed = UnionType.mixup(mixed, typeMap.getType(value));
                }
            }
            return mixed;
        }

        private boolean bothOfType(BinaryOp op, IRType... expected) {
            boolean left = isType(op.getLeft(), expected);
            boolean right = isType(op.getRight(), expected);
            return left && right;
        }

        private boolean anyOfType(BinaryOp op, IRType... expected) {
            boolean left = isType(op.getLeft(), expected);
            boolean right = isType(op.getRight(), expected);
            return left || right;
        }

        private boolean isType(IRValue value, IRType... expected) {
            IRType type = typeMap.getType(value);
            for (IRType expectedType : expected) {
                if (type.equals(expectedType)) {
                    return true;
                }
            }
            return false;
        }

    }

}
