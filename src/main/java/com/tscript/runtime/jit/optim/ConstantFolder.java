package com.tscript.runtime.jit.optim;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.traversal.Traverser;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ConstantFolder implements Optimizer {

    private interface Operation<L, R>{
        Expression operate(L l, R r);
    }

    private static final HashMap<Opcode, HashMap<Class<? extends Expression>, HashMap<Class<? extends Expression>, Operation>>> operationTable = new HashMap<>();
    private static <L extends Expression, R extends Expression> void addOperation(Opcode opcode, Class<L> first, Class<R> second, Operation<L, R> operation){
        HashMap<Class<? extends Expression>, HashMap<Class<? extends Expression>, Operation>> operationAssociations;

        if (operationTable.containsKey(opcode)){
            operationAssociations = operationTable.get(opcode);
        }
        else {
            operationAssociations = new HashMap<>();
            operationTable.put(opcode, operationAssociations);
        }

        if (operationAssociations.containsKey(first)){
            HashMap<Class<? extends Expression>, Operation> operationsPerType = operationAssociations.get(first);
            operationsPerType.put(second, operation);
        }
        else {
            HashMap<Class<? extends Expression>, Operation> operationsPerType = new HashMap<>();
            operationsPerType.put(second, operation);
            operationAssociations.put(first, operationsPerType);
        }

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
    private final boolean dumbMode;

    public ConstantFolder(boolean dumbMode) {
        this.dumbMode = dumbMode;
    }

    @Override
    public Node optimize(Node graph) {
        optimized = false;
        Traverser.traverse(new Trav(), new Handler(), graph);
        return graph;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }


    private static class Trav extends TreeTraverseStrategy {
        @Override
        public boolean isThenFirst(BranchNode node, Context context) {
            return true;
        }
    }


    private class Handler extends ParentDelegationScanner<Void> implements ExpressionVisitor<Expression, Void> {

        private final State<Map<Integer, Expression>> locals = new State<>(new HashMap<>());
        private final Map<Integer, Expression> temps = new HashMap<>();

        private void forgetLocals(){
            locals.get().replaceAll((i, t) -> null);
        }

        private void forgetTemps(){
            temps.replaceAll((i, t) -> null);
        }

        @Override
        public Void visitPushFrame(PushFrame expr, Expression unused) {
            scan(expr.called, expr);
            locals.storeAndSet(new HashMap<>());
            return null;
        }

        @Override
        public Void visitPushSuperFrame(PushSuperFrame expr, Expression unused) {
            locals.storeAndSet(new HashMap<>());
            return null;
        }

        @Override
        public Void visitPopFrame(PopFrame expr, Expression expression) {
            locals.restore();
            if (temps.get(0) != null){
                temps.put(0, validateFoldable(temps.get(0)));
            }
            return null;
        }

        @Override
        public Void visitStoreTemp(StoreTemp expr, Expression parent) {
            scan(expr.what, expr);
            temps.put(expr.index, validateFoldable(expr.what));
            return null;
        }

        @Override
        public Void visitLoadTemp(LoadTemp expr, Expression parent) {
            int address = expr.index;
            if (!temps.containsKey(address))
                temps.put(address, null);
            Expression value = temps.get(address);
            if (value != null && parent != null) {
                parent.replace(expr, value);
                optimized = true;
            }
            return null;
        }

        @Override
        public Void visitStoreLocal(StoreLocal expr, Expression expression) {
            scan(expr.what, expr);
            locals.get().put(expr.index, validateFoldable(expr.what));
            return null;
        }

        @Override
        public Void visitLoadLocal(LoadLocal expr, Expression parent) {
            int address = expr.index();
            if (!locals.get().containsKey(address))
                locals.get().put(address, null);
            Expression value = locals.get().get(address);
            if (value != null && parent != null) {
                parent.replace(expr, value);
                optimized = true;
            }
            return null;
        }

        @Override
        public Void visitBinaryOp(BinaryOp expr, Expression parent) {
            scan(expr.lhs, expr);
            scan(expr.rhs, expr);

            if (parent != null) {
                if (expr.lhs instanceof Str s && expr.rhs instanceof LiteralExpr<?> l){
                    Str stringTree = new Str(s.value + l.value);
                    parent.replace(expr, stringTree);
                    optimized = true;
                    return null;
                }
                else if (expr.rhs instanceof Str s && expr.lhs instanceof LiteralExpr<?> l){
                    Str stringTree = new Str(l.value + s.value);
                    parent.replace(expr, stringTree);
                    optimized = true;
                    return null;
                }

                HashMap<Class<? extends Expression>, Operation> map = operationTable.get(expr.opcode).get(expr.lhs.getClass());
                if (map != null){
                    Operation operation = map.get(expr.rhs.getClass());
                    if (operation != null){
                        Expression folded = operation.operate(expr.lhs, expr.rhs);
                        parent.replace(expr, folded);
                        optimized = true;
                    }
                }
            }

            return null;
        }

        @Override
        public Void visitBoolGuard(BoolGuard expr, Expression expression) {
            super.visitBoolGuard(expr, expression);
            // enforce snapshotting locals before guard evaluation
            forgetLocals();
            return null;
        }

        @Override
        public Void visitTypeGuard(TypeGuard expr, Expression expression) {
            super.visitTypeGuard(expr, expression);
            // enforce snapshotting locals before guard evaluation
            forgetLocals();
            return null;
        }

        @Override
        public Void visitItrNextGuard(ItrNextGuard expr, Expression expression) {
            super.visitItrNextGuard(expr, expression);
            // enforce snapshotting locals before guard evaluation
            forgetLocals();
            return null;
        }

        private static Expression validateFoldable(Expression exp){
            if (Set.of(This.class, ItrNext.class).contains(exp.getClass())) return null;
            return exp;
        }
    }

}
