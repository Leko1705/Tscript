package com.tscript.runtime.jit.optim;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.expr.Null;
import com.tscript.runtime.jit.graph.expr.Range;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.typing.Type;
import com.tscript.runtime.utils.Tuple;

import java.util.*;

/**
 * Optimizers with focus on type optimizations.
 * This includes removing guards if possible, inlining constant type members
 * and access transforming to access by index.
 */
public class TypeReducer implements Optimizer {

    private boolean optimized;
    private Node prev;

    @Override
    public Node optimize(Node graph) {
        optimized = false;

        Context context = new Context(graph);
        Handler handler = new Handler();
        Trav trav = new Trav();

        while (context.getNext() != null){
            Node current = context.getNext();
            Expression expr = current.getExpr();
            expr.accept(handler);
            prev = current;
            current.step(trav, context);
        }

        return graph;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }

    private static class Trav extends TreeTraverseStrategy {
        @Override
        public boolean isThenFirst(BranchNode node, Context context) {
            return false;
        }
    }

    private class Handler extends ParentDelegationScanner<Value> {

        private final State<Map<Integer, Value>> locals = new State<>(new HashMap<>());
        private final Map<Integer, Value> temps = new HashMap<>();

        public Handler(){
            super(Value.UNKNOWN);
        }

        @Override
        public Value visitPushFrame(PushFrame expr, Expression expression) {
            super.visitPushFrame(expr, expression);
            locals.storeAndSet(new HashMap<>());
            return Value.UNKNOWN;
        }

        @Override
        public Value visitPushSuperFrame(PushSuperFrame expr, Expression expression) {
            super.visitPushSuperFrame(expr, expression);
            locals.storeAndSet(new HashMap<>());
            return Value.UNKNOWN;
        }

        @Override
        public Value visitPopFrame(PopFrame expr, Expression expression) {
            locals.restore();
            return Value.UNKNOWN;
        }

        @Override
        public Value visitNull(Null expr, Expression expression) {
            return Value.NULL;
        }

        @Override
        public Value visitInt(Int expr, Expression expression) {
            return new Value(TInteger.TYPE, expr.value, expr.value != 0, false, false);
        }

        @Override
        public Value visitReal(Real expr, Expression expression) {
            return new Value(TReal.TYPE, expr.value, expr.value != 0, false, false);
        }

        @Override
        public Value visitString(Str expr, Expression expression) {
            return new Value(TString.TYPE, expr.value, !expr.value.isEmpty(), true, false);
        }

        @Override
        public Value visitBool(Bool expr, Expression expression) {
            return expr.value ? Value.TRUE : Value.FALSE;
        }

        @Override
        public Value visitArray(Array expr, Expression expression) {
            List<Value> contents = new ArrayList<>();
            for (Expression exp : expr.contents()){
                contents.add(scan(exp, expression));
            }
            return new Value(TArray.TYPE, contents, !contents.isEmpty(), true, false);
        }

        @Override
        public Value visitDict(Dict expr, Expression expression) {
            List<Value> keys = new ArrayList<>();
            List<Value> values = new ArrayList<>();

            for (Expression key : expr.keys){
                keys.add(scan(key, expression));
            }

            for (Expression value : expr.values){
                values.add(scan(value, expression));
            }

            Tuple<List<Value>, List<Value>> dictRep = new Tuple<>(keys, values);

            return new Value(TDictionary.TYPE, dictRep, !expr.keys.isEmpty(), true, false);
        }

        @Override
        public Value visitCallInp(CallInp expr, Expression expression) {
            super.visitCallInp(expr, expression);
            return Value.UNKNOWN;
        }

        @Override
        public Value visitCallMap(CallMap expr, Expression expression) {
            super.visitCallMap(expr, expression);
            return Value.UNKNOWN;
        }


        @Override
        public Value visitRange(Range expr, Expression expression) {
            Value lhs = scan(expr.from, expr);
            Value rhs = scan(expr.from, expr);
            Object val = null;
            if (lhs != Value.UNKNOWN && rhs != Value.UNKNOWN) {
                val = new Tuple<>(lhs, rhs);
            }
            return new Value(com.tscript.runtime.typing.Range.TYPE, val, false, true, false);
        }

        @Override
        public Value visitGetType(GetType expr, Expression expression) {
            Value val = super.visitGetType(expr, expression);
            if (val == null) return new Value(Type.TYPE, null, true, false, true);
            if (val.type == Type.TYPE) return Value.TYPE;
            return new Value(Type.TYPE, val.type, true, false, true);
        }

        @Override
        public Value visitNative(Native expr, Expression expression) {
            super.visitNative(expr, expression);
            return new Value(Function.TYPE, "N" + expr.name, true, false, true);
        }

        @Override
        public Value visitMethod(Method expr, Expression expression) {
            Value func = scan(expr.underlying, expr);
            scan(expr.owner, expr);
            return func;
        }

        @Override
        public Value visitAbstract(Abstract expr, Expression expression) {
            return new Value(Function.TYPE, "A" + expr.name, true, false, true);
        }

        @Override
        public Value visitType(com.tscript.runtime.jit.graph.expr.Type expr, Expression expression) {
            return new Value(Type.TYPE, expr.index(), true, false, true);
        }

        @Override
        public Value visitVirtual(Virtual expr, Expression expression) {
            return new Value(Function.TYPE, "V" + expr.index(), true, false, true);
        }

        @Override
        public Value visitBuiltin(Builtin expr, Expression expression) {
            TObject obj = Builtins.load(expr.index());
            if (obj instanceof Type){
                return new Value(Type.TYPE, obj.getType(), true, false, true);
            }
            else {
                return new Value(Function.TYPE, "B" + expr.index(), true, false, true);
            }
        }

        @Override
        public Value visitNot(Not expr, Expression expression) {
            Value val =  super.visitNot(expr, expression);
            if (val == Value.UNKNOWN) return Value.UNKNOWN;
            if (val.type == TInteger.TYPE) {
                if (expr.canFail) {
                    expr.canFail = false;
                    optimized = true;
                }
                if (val.value == null) return val;
                int value = (int) val.value;
                return new Value(TInteger.TYPE, ~value, value != 0, false, false);
            }
            else if (val.type == TBoolean.TYPE) {
                if (expr.canFail) {
                    expr.canFail = false;
                    optimized = true;
                }
                if (val.value == null) return val;
                boolean value = !((boolean) val.value);
                return value ? Value.TRUE : Value.FALSE;
            }
            return Value.UNKNOWN;
        }

        @Override
        public Value visitNeg(Neg expr, Expression expression) {
            Value val =  super.visitNeg(expr, expression);
            if (val == Value.UNKNOWN) return Value.UNKNOWN;
            if (val.type == TInteger.TYPE) {
                if (expr.canFail) {
                    expr.canFail = false;
                    optimized = true;
                }
                if (val.value == null) return val;
                int value = (int) val.value;
                return new Value(TInteger.TYPE, -value, value != 0, false, false);
            }
            else if (val.type == TReal.TYPE) {
                if (expr.canFail) {
                    expr.canFail = false;
                    optimized = true;
                }
                if (val.value == null) return val;
                double value = (double) val.value;
                return new Value(TReal.TYPE, -value, value != 0, false, false);
            }
            return Value.UNKNOWN;
        }

        @Override
        public Value visitEquality(Equality expr, Expression expression) {
            Value lhs = scan(expr.lhs, expr);
            Value rhs = scan(expr.rhs, expr);
            if (lhs == Value.UNKNOWN || rhs == Value.UNKNOWN) return Value.UNKNOWN;
            return Objects.equals(lhs, rhs) ? Value.TRUE : Value.FALSE;
        }

        @Override
        public Value visitStoreTemp(StoreTemp expr, Expression expression) {
            Value val = scan(expr.what, expr);
            temps.put(expr.index, val);
            return val;
        }

        @Override
        public Value visitLoadTemp(LoadTemp expr, Expression expression) {
            return temps.computeIfAbsent(expr.index, k -> Value.UNKNOWN);
        }

        @Override
        public Value visitStoreLocal(StoreLocal expr, Expression expression) {
            Value val = scan(expr.what, expr);
            locals.get().put(expr.index, val);
            return val;
        }

        @Override
        public Value visitLoadLocal(LoadLocal expr, Expression expression) {
            return locals.get().computeIfAbsent(expr.index, k -> Value.UNKNOWN);
        }

        @Override
        public Value visitContainerRead(ContainerRead expr, Expression expression) {
            Value container = scan(expr.container, expr);
            Value key = scan(expr.key, expr);

            if (container == Value.UNKNOWN || key == Value.UNKNOWN
                    || container.value == null || key.value == null)
                return Value.UNKNOWN;

            if (container.type == TArray.TYPE) {
                @SuppressWarnings("unchecked") List<Value> values = (List<Value>) container.value;

                if (key.type == TInteger.TYPE) {
                    int index = (int) key.value;
                    return values.get(index);
                }
            }

            else if (container.type == TDictionary.TYPE) {
                @SuppressWarnings("unchecked") Tuple<List<Value>, List<Value>> tuple = (Tuple<List<Value>, List<Value>>) container.value;
                for (int i = 0; i < tuple.getSecond().size(); i++) {
                    Value dictKey = tuple.getFirst().get(i);
                    Value value = tuple.getSecond().get(i);

                    if (dictKey.equals(key)) return value;
                }
            }

            return Value.UNKNOWN;
        }

        @Override
        public Value visitContainerWrite(ContainerWrite expr, Expression expression) {
            Value container = scan(expr.container, expr);
            Value key = scan(expr.key, expr);
            Value value = scan(expr.value, expr);

            if (container == Value.UNKNOWN || key == Value.UNKNOWN || value == Value.UNKNOWN
                    || container.value == null || key.value == null || value == null)
                return value;

            if (container.type == TArray.TYPE) {
                @SuppressWarnings("unchecked") List<Value> values = (List<Value>) container.value;

                if (key.type == TInteger.TYPE) {
                    int index = (int) key.value;
                    values.set(index, value);
                    return value;
                }
            }

            else if (container.type == TDictionary.TYPE) {
                @SuppressWarnings("unchecked") Tuple<List<Value>, List<Value>> tuple = (Tuple<List<Value>, List<Value>>) container.value;
                for (int i = 0; i < tuple.getSecond().size(); i++) {
                    Value dictKey = tuple.getFirst().get(i);

                    if (dictKey.equals(key)) {
                        tuple.getSecond().set(i, value);
                        return value;
                    }
                }
            }

            return value;
        }

        @Override
        public Value visitBoolGuard(BoolGuard expr, Expression parent) {
            Value val = super.visitBoolGuard(expr, parent);

            if (val.isTrue != null && val.isTrue){
                removeCurrentNode(expr, parent, expr.expression);
            }

            return val;
        }

        @Override
        public Value visitTypeGuard(TypeGuard expr, Expression parent) {
            Value val = super.visitTypeGuard(expr, parent);
            if (val == Value.UNKNOWN) return val;
            for (Type type : expr.required){
                if (val.type.equals(type)){
                    removeCurrentNode(expr, parent, expr.expression);
                    break;
                }
            }
            return val;
        }

    }


    private void removeCurrentNode(Expression current, Expression parent, Expression replacement){
        optimized = true;
        if (parent != null && replacement != null){
            parent.replace(current, replacement);
            return;
        }
        if (prev.getNext().getNext() != null){
            prev.setNext(prev.getNext().getNext());
            return;
        }
        throw new AssertionError(
                "either non-last node or expression replacement; but not both, or both missing");
    }


    private static class Value {

        private static final Value NULL = new Value(com.tscript.runtime.typing.Null.TYPE, null, false, false, false);

        private static final Value UNKNOWN = new Value() {
            @Override public boolean equals(Object obj) {
                return false;
            }
        };

        private static final Value TYPE = new Value(com.tscript.runtime.typing.Type.TYPE, new Object(), true, false, true);

        private static final Value TRUE = new Value(TBoolean.TYPE, true, true, false, false);
        private static final Value FALSE = new Value(TBoolean.TYPE, false, false, false, false);

        public Type type;
        public boolean iterable = false;
        public Boolean isTrue = false;
        public boolean isCallable = false;
        public Object value;

        private Value() {}

        public Value(Type type, Object value, Boolean isTrue, boolean iterable, boolean isCallable) {
            this.type = type;
            this.value = value;
            this.isTrue = isTrue;
            this.iterable = iterable;
            this.isCallable = isCallable;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Value that)) return false;
            if (obj == UNKNOWN || this.value == null || that.value == null) return false;
            return Objects.equals(this.value, that.value);
        }

    }

}
