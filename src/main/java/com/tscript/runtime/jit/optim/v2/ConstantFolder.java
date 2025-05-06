package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.caching.TypeCache;
import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.*;
import com.tscript.runtime.jit.ir.nodes.Range;
import com.tscript.runtime.jit.ir.nodes.Type;
import com.tscript.runtime.jit.ir.types.*;
import com.tscript.runtime.jit.optim.State;
import com.tscript.runtime.typing.*;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ConstantFolder extends IRParentDelegationScanner<Void> implements IROptimizer {

    private interface Operation<L, R>{
        IRValue operate(L l, R r);
    }

    private static final HashMap<Opcode, HashMap<Class<? extends IRValue>, HashMap<Class<? extends IRValue>, Operation>>>
            operationTable = new HashMap<>();

    private static <L extends IRValue, R extends IRValue> void addOperation(Opcode opcode, Class<L> first, Class<R> second, Operation<L, R> operation){
        var operationAssociations = operationTable.computeIfAbsent(opcode, k -> new HashMap<>());
        var operationsPerType = operationAssociations.computeIfAbsent(first, k -> new HashMap<>());
        operationsPerType.put(second, operation);
    }

    static {
        init();
    }

    private static void init(){
        addOperation(Opcode.ADD, Int.class, Int.class, (i1, i2) -> new Int(i1.value + i2.value));
        addOperation(Opcode.ADD, Int.class, Real.class, (i1, i2) -> new Real(i1.value + i2.value));
        addOperation(Opcode.ADD, Real.class, Int.class, (i1, i2) -> new Real(i1.value + i2.value));
        addOperation(Opcode.ADD, Real.class, Real.class, (i1, i2) -> new Real(i1.value + i2.value));

        addOperation(Opcode.SUB, Int.class, Int.class, (i1, i2) ->new Int(i1.value - i2.value));
        addOperation(Opcode.SUB, Int.class, Real.class, (i1, i2) ->new Real(i1.value - i2.value));
        addOperation(Opcode.SUB, Real.class, Int.class, (i1, i2) ->new Real(i1.value - i2.value));
        addOperation(Opcode.SUB, Real.class, Real.class, (i1, i2) ->new Real(i1.value - i2.value));

        addOperation(Opcode.MUL, Int.class, Int.class, (i1, i2) -> new Int(i1.value * i2.value));
        addOperation(Opcode.MUL, Real.class, Int.class, (i1, i2) -> new Real(i1.value * i2.value));
        addOperation(Opcode.MUL, Int.class, Real.class, (i1, i2) -> new Real(i1.value * i2.value));
        addOperation(Opcode.MUL, Real.class, Real.class, (i1, i2) -> new Real(i1.value * i2.value));

        addOperation(Opcode.DIV, Int.class, Int.class, (i1, i2) -> new Real((double) i1.value / i2.value));
        addOperation(Opcode.DIV, Real.class, Int.class, (i1, i2) -> new Real((double) i1.value / i2.value));
        addOperation(Opcode.DIV, Int.class, Real.class, (i1, i2) -> new Real((double) i1.value / i2.value));
        addOperation(Opcode.DIV, Real.class, Real.class, (i1, i2) -> new Real((double) i1.value / i2.value));

        addOperation(Opcode.IDIV, Int.class, Int.class, (i1, i2) -> new Int(i1.value / i2.value));
        addOperation(Opcode.MOD, Int.class, Int.class, (i1, i2) -> new Int(i1.value % i2.value));

        addOperation(Opcode.POW, Int.class, Int.class, (i1, i2) -> new Int((int) Math.pow(i1.value, i2.value)));
        addOperation(Opcode.POW, Real.class, Int.class, (i1, i2) -> new Real(Math.pow(i1.value, i2.value)));
        addOperation(Opcode.POW, Int.class, Real.class, (i1, i2) -> new Real(Math.pow(i1.value, i2.value)));
        addOperation(Opcode.POW, Real.class, Real.class, (i1, i2) -> new Real(Math.pow(i1.value, i2.value)));

        addOperation(Opcode.SLA, Int.class, Int.class, (i1, i2) -> new Int( i1.value << i2.value));
        addOperation(Opcode.SRA, Int.class, Int.class, (i1, i2) -> new Int( i1.value >> i2.value));
        addOperation(Opcode.SRL, Int.class, Int.class, (i1, i2) -> new Int(i1.value >>> i2.value));

        addOperation(Opcode.LT, Int.class, Int.class, (i1, i2) -> new Bool(i1.value < i2.value));
        addOperation(Opcode.LT, Int.class, Real.class, (i1, i2) -> new Bool(i1.value < i2.value));
        addOperation(Opcode.LT, Real.class, Int.class, (i1, i2) -> new Bool(i1.value < i2.value));
        addOperation(Opcode.LT, Real.class, Real.class, (i1, i2) -> new Bool(i1.value < i2.value));

        addOperation(Opcode.GT, Int.class, Int.class, (i1, i2) -> new Bool(i1.value > i2.value));
        addOperation(Opcode.GT, Int.class, Real.class, (i1, i2) -> new Bool(i1.value > i2.value));
        addOperation(Opcode.GT, Real.class, Int.class, (i1, i2) -> new Bool(i1.value > i2.value));
        addOperation(Opcode.GT, Real.class, Real.class, (i1, i2) -> new Bool(i1.value > i2.value));

        addOperation(Opcode.LEQ, Int.class, Int.class, (i1, i2) -> new Bool(i1.value <= i2.value));
        addOperation(Opcode.LEQ, Int.class, Real.class, (i1, i2) -> new Bool(i1.value <= i2.value));
        addOperation(Opcode.LEQ, Real.class, Int.class, (i1, i2) -> new Bool(i1.value <= i2.value));
        addOperation(Opcode.LEQ, Real.class, Real.class, (i1, i2) -> new Bool(i1.value <= i2.value));

        addOperation(Opcode.GEQ, Int.class, Int.class, (i1, i2) -> new Bool(i1.value >= i2.value));
        addOperation(Opcode.GEQ, Int.class, Real.class, (i1, i2) -> new Bool(i1.value >= i2.value));
        addOperation(Opcode.GEQ, Real.class, Int.class, (i1, i2) -> new Bool(i1.value >= i2.value));
        addOperation(Opcode.GEQ, Real.class, Real.class, (i1, i2) -> new Bool(i1.value >= i2.value));

        addOperation(Opcode.AND, Int.class, Int.class, (i1, i2) -> new Int( i1.value & i2.value));
        addOperation(Opcode.AND, Bool.class, Bool.class, (i1, i2) -> new Bool(i1.value && i2.value));

        addOperation(Opcode.OR, Int.class, Int.class, (i1, i2) -> new Int( i1.value | i2.value));
        addOperation(Opcode.OR, Bool.class, Bool.class, (i1, i2) -> new Bool(i1.value || i2.value));

        addOperation(Opcode.XOR, Int.class, Int.class, (i1, i2) -> new Int( i1.value ^ i2.value));
        addOperation(Opcode.XOR, Bool.class, Bool.class, (i1, i2) -> new Bool(i1.value ^ i2.value));
    }



    private boolean optimized;

    private State<Map<Integer, IRValue>> locals;
    private Map<Integer, IRValue> temps;

    @Override
    public IRBlock optimize(IRBlock block) {
        optimized = false;

        locals = new State<>(new HashMap<>());
        temps = new HashMap<>();

        IRWalker<IRNode, Void> walker = new IRTreeWalker<>();
        walker.walk(this, block);

        return block;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }

    private void forgetLocals(){
        locals.get().replaceAll((i, t) -> null);
    }


    @Override
    public Void visitPushFrame(PushFrame node, IRNode parent) {
        scan(node.called, node);
        locals.storeAndSet(new HashMap<>());
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, IRNode parent) {
        locals.restore();
        if (temps.get(0) != null){
            temps.put(0, validateFoldable(temps.get(0)));
        }
        return null;
    }

    @Override
    public Void visitStoreTemp(StoreTemp node, IRNode parent) {
        scan(node.value, node);
        temps.put(node.address, validateFoldable(node.value));
        return null;
    }

    @Override
    public Void visitLoadTemp(LoadTemp node, IRNode parent) {
        IRValue value = temps.computeIfAbsent(node.address, k -> null);
        if (value != null && parent != null) {
            optimized |= parent.replace(node, value);
        }
        return null;
    }


    @Override
    public Void visitStoreLocal(StoreLocal node, IRNode parent) {
        scan(node.value, node);
        locals.get().put(node.ssaAddress, validateFoldable(node.value));
        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal node, IRNode parent) {
        IRValue value = locals.get().computeIfAbsent(node.ssaAddress, k -> null);
        if (value != null && parent != null) {
            optimized |= parent.replace(node, value);
        }
        return null;
    }

    @Override
    public Void visitGuard(Guard node, IRNode parent) {
        scan(node.condition, node);
        forgetLocals();
        return null;
    }

    @Override
    public Void visitGetType(GetType node, IRNode parent) {
        scan(node.value, node);

        IRValue value = valueOf(node.value);

        if (isType(value, IRIntegerType.class)) {
            optimized |= parent.replace(node, toTypeNode(TInteger.TYPE));
        }
        else if (isType(value, IRRealType.class)) {
            optimized |= parent.replace(node, toTypeNode(TReal.TYPE));
        }
        else if (isType(value, IRBooleanType.class)) {
            optimized |= parent.replace(node, toTypeNode(TBoolean.TYPE));
        }
        else if (isType(value, IRStringType.class)) {
            optimized |= parent.replace(node, toTypeNode(TString.TYPE));
        }
        else if (isType(value, IRNullType.class)) {
            optimized |= parent.replace(node, toTypeNode(Null.TYPE));
        }
        else if (isType(value, IRArrayType.class)) {
            optimized |= parent.replace(node, toTypeNode(TArray.TYPE));
        }
        else if (isType(value, IRDictionaryType.class)) {
            optimized |= parent.replace(node, toTypeNode(TDictionary.TYPE));
        }
        else if (isType(value, IRRangeType.class)) {
            optimized |= parent.replace(node, toTypeNode(com.tscript.runtime.typing.Range.TYPE));
        }
        else if (isType(value, IRTypeType.class) && value.getType() == IRTypeType.TYPE_TYPE) {
            optimized |= parent.replace(node, toTypeNode(com.tscript.runtime.typing.Type.TYPE));
        }

        return null;
    }

    @Override
    public Void visitEquals(Equals node, IRNode parent) {
        scan(node.lhs, node);
        scan(node.rhs, node);

        IRValue lhs = valueOf(node.lhs);
        IRValue rhs = valueOf(node.rhs);

        if (lhs != IRUnknownType.INSTANCE && rhs != IRUnknownType.INSTANCE) {
            if (!isType(lhs, lhs.getType().getClass())) {
                optimized |= parent.replace(node, new WrapTObj(new Bool(!node.shouldBeEqual)));
                return null;
            }
        }

        if (lhs instanceof Literal<?> l && rhs instanceof Literal<?> r) {
            optimized |= parent.replace(node, new WrapTObj(new Bool(Objects.equals(l.value, r.value) == node.shouldBeEqual)));
        }
        else if (lhs instanceof Type lt && rhs instanceof Type rt) {
            boolean sameImportStrategy = (lt.fromModule == rt.fromModule);
            boolean sameAddress = (lt.address == rt.address);
            optimized |= parent.replace(node, new WrapTObj(new Bool((sameImportStrategy && sameAddress) == node.shouldBeEqual)));
        }

        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp node, IRNode parent) {
        scan(node.lhs, node);
        scan(node.rhs, node);

        IRValue lhs = valueOf(node.lhs);
        IRValue rhs = valueOf(node.rhs);

        if (parent != null) {
            if (lhs instanceof Str s && rhs instanceof Literal<?> l) {
                Str str = new Str(s.value + l.value);
                optimized |= parent.replace(node, new WrapTObj(str));
                return null;
            }
            if (rhs instanceof Str s && lhs instanceof Literal<?> l) {
                Str str = new Str(l.value + s.value);
                optimized |= parent.replace(node, new WrapTObj(str));
                return null;
            }

            var lhsOpMap = operationTable.get(node.operation);
            if (lhsOpMap != null) {
                var rhsOpMap = lhsOpMap.get(lhs.getClass());
                if (rhsOpMap != null) {
                    Operation operation = rhsOpMap.get(rhs.getClass());
                    if (operation != null) {
                        IRValue folded = operation.operate(lhs, rhs);
                        optimized |= parent.replace(node, new WrapTObj(folded));
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Void visitNeg(Neg node, IRNode parent) {
        scan(node.value, node);

        IRValue value = valueOf(node.value);

        if (value instanceof Int i) {
            Int folded = new Int(-i.value);
            optimized |= parent.replace(node, new WrapTObj(folded));
        }
        else if (value instanceof Real r) {
            Real folded = new Real(-r.value);
            optimized |= parent.replace(node, new WrapTObj(folded));
        }

        return null;
    }

    @Override
    public Void visitNot(Not node, IRNode parent) {
        scan(node.value, node);

        IRValue value = valueOf(node.value);

        if (value instanceof Int i) {
            Int folded = new Int(~i.value);
            optimized |= parent.replace(node, new WrapTObj(folded));
        }
        else if (value instanceof Bool b) {
            Bool folded = new Bool(!b.value);
            optimized |= parent.replace(node, new WrapTObj(folded));
        }

        return null;
    }

    @Override
    public Void visitCallInp(CallInp node, IRNode parent) {
        super.visitCallInp(node, parent);

        if (node.called instanceof Builtin builtin) {

            if (node.args.size() == 1) {
                TObject builtinObj = Builtins.load(builtin.id);
                IRValue arg = valueOf(node.args.get(0));
                optimized |= foldOneArgBuiltin(node, parent, builtinObj, arg);
            }
        }

        return null;
    }

    private boolean foldOneArgBuiltin(IRValue node, IRNode parent, TObject builtinObj, IRValue arg) {
        if (builtinObj == Null.TYPE) {
            return parent.replace(node, new WrapTObj(new NullValue()));
        }

        if (builtinObj == TInteger.TYPE) {
            if (arg instanceof Int) {
                return parent.replace(node, new WrapTObj(arg));
            }
            else if (arg instanceof Real real) {
                return parent.replace(node, new WrapTObj(new Int(real.value.intValue())));
            }
            else if (arg instanceof Str str) {
                try {
                    return parent.replace(node, new WrapTObj(new Int(Integer.parseInt(str.value))));
                }
                catch (NumberFormatException ignored) {}
            }
        }

        else if (builtinObj == TReal.TYPE) {
            if (arg instanceof Real) {
                return parent.replace(node, new WrapTObj(arg));
            }
            else if (arg instanceof Int i) {
                return parent.replace(node, new WrapTObj(new Real(i.value.doubleValue())));
            }
            else if (arg instanceof Str str) {
                try {
                    return parent.replace(node, new WrapTObj(new Real(Double.parseDouble(str.value))));
                }
                catch (NumberFormatException ignored) {}
            }
        }

        else if (builtinObj == TBoolean.TYPE) {
            if (arg instanceof Bool) {
                return parent.replace(node, new WrapTObj(arg));
            }
            else if (arg instanceof NullValue) {
                return parent.replace(node, new WrapTObj(new Bool(false)));
            }
            else if (arg instanceof Int i) {
                return parent.replace(node, new WrapTObj(new Bool(i.value != 0)));
            }
            else if (arg instanceof Str str) {
                return parent.replace(node, new WrapTObj(new Bool(!str.value.isEmpty())));
            }
            else if (arg instanceof Array arr) {
                return parent.replace(node, new WrapTObj(new Bool(!arr.content.isEmpty())));
            }
            else if (arg instanceof Type || arg instanceof Virtual
                    || arg instanceof Builtin || arg instanceof Native
                    || arg instanceof Real) {
                return parent.replace(node, new WrapTObj(new Bool(true)));
            }
        }

        else if (builtinObj == TString.TYPE) {
            String s = Stringifier.toString(arg);
            if (s != null) {
                return parent.replace(node, new WrapTObj(new Str(s)));
            }
        }

        return false;
    }

    private static IRValue validateFoldable(IRValue exp) {
        if (Set.of(This.class, ItrNext.class).contains(exp.getClass())) return null;
        return exp;
    }

    private static class Stringifier extends IRVisitorImpl<Void, String> {

        public static String toString(IRValue value) {
            return value.accept(new Stringifier());
        }

        @Override
        public String visitInteger(Int node, Void unused) {
            return node.value.toString();
        }

        @Override
        public String visitReal(Real node, Void unused) {
            return node.value.toString();
        }

        @Override
        public String visitBool(Bool node, Void unused) {
            return node.value.toString();
        }

        @Override
        public String visitString(Str node, Void unused) {
            return node.value;
        }

        @Override
        public String visitNull(NullValue node, Void unused) {
            return "null";
        }

        @Override
        public String visitRange(Range node, Void unused) {
            String l = node.from.accept(this);
            if (l == null) return null;
            String r = node.to.accept(this);
            if (r == null) return null;
            return l + ":" + r;
        }

        @Override
        public String visitArray(Array node, Void unused) {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (IRValue value : node.content) {
                String s = value.accept(this);
                if (s == null) return null;
                joiner.add(s);
            }
            return joiner.toString();
        }

        @Override
        public String visitDictionary(Dict node, Void unused) {
            StringJoiner joiner = new StringJoiner(",", "{", "}");
            for (int i = 0; i < node.values.size(); i++) {
                String key = node.keys.get(i).accept(this);
                if (key == null) return null;
                String value = node.values.get(i).accept(this);
                if (value == null) return null;
                joiner.add(key + ": " + value);
            }
            return joiner.toString();
        }

        @Override
        public String visitNative(Native node, Void unused) {
            return "Function<" + node.name + ">";
        }

        @Override
        public String visitSetOwner(SetOwner node, Void unused) {
            return node.member.accept(this);
        }

        @Override
        public String visitBuiltin(Builtin node, Void unused) {
            return Builtins.load(node.id).getDisplayName();
        }

        @Override
        public String visitUnpackObj(UnpackObj node, Void unused) {
            return node.value.accept(this);
        }

        @Override
        public String visitWrapTObj(WrapTObj node, Void unused) {
            return node.value.accept(this);
        }
    }

    private static IRValue valueOf(IRValue value) {
        if (value instanceof WrapTObj w) {
            return w.value;
        }
        if (value instanceof UnpackObj u) {
            return u.value;
        }
        return value;
    }

    private static Type toTypeNode(com.tscript.runtime.typing.Type type) {
        return new Type(TypeCache.getId(type), false);
    }


    private boolean isType(IRValue value, Class<? extends IRType> type) {
        return value.getType().accept(new IsIRType(), type);
    }

    private static class IsIRType extends IRTypeVisitorImpl<Class<? extends IRType>, Boolean> {
        @Override
        public Boolean defaultOperation(IRType type, Class<? extends IRType> aClass) {
            return type.getClass() == aClass;
        }
    }

}
