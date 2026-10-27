package com.tscript.runtime.jit.comp.analysis;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.BoolType;
import com.tscript.runtime.jit.comp.ir.types.IntType;
import com.tscript.runtime.jit.comp.ir.types.RealType;
import com.tscript.runtime.jit.comp.ir.types.StrType;
import com.tscript.runtime.typing.TObject;

public class Boxing {
    
    public static boolean isBoxed(IRValue value, Graph graph) {
        return value.accept(new Infer(graph.typeMap()));
    }

    public static boolean isPrimitive(IRValue value, Graph graph) {
        return !isBoxed(value, graph);
    }

    private record Infer(Graph.TypeMap typeMap) implements IRValueVisitor<Void, Boolean> {


        @Override
        public Boolean visitInteger(Int node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitReal(Real node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitString(Str node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitBool(Bool node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitNull(NullValue node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitArray(Array node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitDict(Dict node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitRange(Range node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitThis(This node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitAdd(Add node, Void unused) {
            IRType lhs = typeMap.getType(node.getLeft());
            IRType rhs = typeMap.getType(node.getRight());
            if (lhs == StrType.INSTANCE || rhs == StrType.INSTANCE) return false;
            return checkNumerical(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitSub(Sub node, Void unused) {
            return checkNumerical(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitMul(Mul node, Void unused) {
            return checkNumerical(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitDiv(Div node, Void unused) {
            return checkNumerical(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitIDiv(IDiv node, Void unused) {
            return checkIntOnly(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitMod(Mod node, Void unused) {
            return checkIntOnly(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitPow(Pow node, Void unused) {
            return checkNumerical(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitShl(Shl node, Void unused) {
            return checkIntOnly(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitShr(Shr node, Void unused) {
            return checkIntOnly(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitUshr(Ushr node, Void unused) {
            return checkIntOnly(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitAnd(And node, Void unused) {
            return checkIntOrBool(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitOr(Or node, Void unused) {
            return checkIntOrBool(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitXor(Xor node, Void unused) {
            return checkIntOrBool(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitNot(Not node, Void unused) {
            return checkIntOrBool(node.getValue());
        }

        @Override
        public Boolean visitNeg(Neg node, Void unused) {
            return checkNumerical(node.getValue());
        }

        @Override
        public Boolean visitPos(Pos node, Void unused) {
            return checkNumerical(node.getValue());
        }

        @Override
        public Boolean visitType(Type node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitTypeof(Typeof node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitGt(Gt node, Void unused) {
            return checkNumerical(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitLt(Lt node, Void unused) {
            return checkNumerical(node.getLeft(), node.getRight());
        }

        @Override
        public Boolean visitEquals(Equals node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitParameter(Parameter node, Void unused) {
            return typeMap.getType(node).getJvmType() == TObject.class;
        }

        @Override
        public Boolean visitBuiltin(Builtin node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitToInpArg(ToInpArg node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitToMapArg(ToMapArg node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitCall(Call node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitToString(ToString node, Void unused) {
            return false;
        }

        @Override
        public Boolean visitPhi(Phi node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitLocal(Local node, Void unused) {
            return true;
        }

        @Override
        public Boolean visitGlobal(Global node, Void unused) {
            return true;
        }

        private boolean checkIntOnly(IRValue left, IRValue right) {
                IRType lhs = typeMap.getType(left);
                IRType rhs = typeMap.getType(right);
                return lhs != IntType.INSTANCE || rhs != IntType.INSTANCE;
        }

        private boolean checkNumerical(IRValue left, IRValue right) {
                IRType lhs = typeMap.getType(left);
                IRType rhs = typeMap.getType(right);
                if (lhs == IntType.INSTANCE && rhs == IntType.INSTANCE) return false;
                if (lhs == RealType.INSTANCE && rhs == IntType.INSTANCE) return false;
                if (lhs == IntType.INSTANCE && rhs == RealType.INSTANCE) return false;
                if (lhs == RealType.INSTANCE && rhs == RealType.INSTANCE) return false;
                return true;
        }

        private boolean checkIntOrBool(IRValue left, IRValue right) {
            IRType lhs = typeMap.getType(left);
            IRType rhs = typeMap.getType(right);
            if (lhs == IntType.INSTANCE && rhs == IntType.INSTANCE) return false;
            if (lhs == BoolType.INSTANCE && rhs == BoolType.INSTANCE) return false;
            return true;
        }

        private boolean checkNumerical(IRValue value) {
            IRType ty = typeMap.getType(value);
            if (ty == IntType.INSTANCE) return false;
            if (ty == RealType.INSTANCE) return false;
            return true;
        }

        private boolean checkIntOrBool(IRValue value) {
            IRType ty = typeMap.getType(value);
            if (ty == IntType.INSTANCE) return false;
            if (ty == BoolType.INSTANCE) return false;
            return true;
        }
    }

}
