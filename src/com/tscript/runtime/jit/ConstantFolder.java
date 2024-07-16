package com.tscript.runtime.jit;

import com.tscript.runtime.jit.BytecodeParser.*;
import com.tscript.tscriptc.generation.Opcode;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ConstantFolder extends ParentDelegationPhase<Void> {

    
    private interface Operation<L, R>{
        Tree operate(L l, R r);
    }

    private static final HashMap<Opcode, HashMap<Class<? extends Tree>, HashMap<Class<? extends Tree>, Operation>>> operationTable = new HashMap<>();
    private static <L extends Tree, R extends Tree> void addOperation(Opcode opcode, Class<L> first, Class<R> second, Operation<L, R> operation){
        HashMap<Class<? extends Tree>, HashMap<Class<? extends Tree>, Operation>> operationAssociations;

        if (operationTable.containsKey(opcode)){
            operationAssociations = operationTable.get(opcode);
        }
        else { 
            operationAssociations = new HashMap<>();
            operationTable.put(opcode, operationAssociations);
        }

        if (operationAssociations.containsKey(first)){
            HashMap<Class<? extends Tree>, Operation> operationsPerType = operationAssociations.get(first);
            operationsPerType.put(second, operation);
        }
        else {
            HashMap<Class<? extends Tree>, Operation> operationsPerType = new HashMap<>();
            operationsPerType.put(second, operation);
            operationAssociations.put(first, operationsPerType);
        }

    }

    static {
        init();
    }

    private static void init(){
        addOperation(Opcode.ADD, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree(i1.value + i2.value));
        addOperation(Opcode.SUB, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree(i1.value - i2.value));
        addOperation(Opcode.MUL, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree(i1.value * i2.value));
        addOperation(Opcode.DIV, IntegerTree.class, IntegerTree.class, (i1, i2) -> new RealTree((double) i1.value / i2.value));
        addOperation(Opcode.IDIV, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree(i1.value / i2.value));
        addOperation(Opcode.MOD, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree(i1.value % i2.value));
        addOperation(Opcode.POW, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree((int) Math.pow(i1.value, i2.value)));

        addOperation(Opcode.SLA, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree( i1.value << i2.value));
        addOperation(Opcode.SRA, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree( i1.value >> i2.value));
        addOperation(Opcode.SLA, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree(i1.value >>> i2.value));

        addOperation(Opcode.LT, IntegerTree.class, IntegerTree.class, (i1, i2) -> new BooleanTree(i1.value < i2.value));
        addOperation(Opcode.LEQ, IntegerTree.class, IntegerTree.class, (i1, i2) -> new BooleanTree(i1.value <= i2.value));
        addOperation(Opcode.GT, IntegerTree.class, IntegerTree.class, (i1, i2) -> new BooleanTree(i1.value > i2.value));
        addOperation(Opcode.GEQ, IntegerTree.class, IntegerTree.class, (i1, i2) -> new BooleanTree(i1.value >= i2.value));

        addOperation(Opcode.AND, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree( i1.value & i2.value));
        addOperation(Opcode.OR, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree( i1.value | i2.value));
        addOperation(Opcode.XOR, IntegerTree.class, IntegerTree.class, (i1, i2) -> new IntegerTree( i1.value ^ i2.value));

        addOperation(Opcode.AND, BooleanTree.class, BooleanTree.class, (i1, i2) -> new BooleanTree(i1.value && i2.value));
        addOperation(Opcode.OR, BooleanTree.class, BooleanTree.class, (i1, i2) -> new BooleanTree(i1.value || i2.value));
        addOperation(Opcode.XOR, BooleanTree.class, BooleanTree.class, (i1, i2) -> new BooleanTree(i1.value ^ i2.value));

        addOperation(Opcode.ADD, IntegerTree.class, RealTree.class, (i, r) -> new RealTree(i.value + r.value));
        addOperation(Opcode.ADD, RealTree.class, IntegerTree.class, (r, i) -> new RealTree(r.value + i.value));

        addOperation(Opcode.SUB, IntegerTree.class, RealTree.class, (i, r) -> new RealTree(i.value - r.value));
        addOperation(Opcode.SUB, RealTree.class, IntegerTree.class, (r, i) -> new RealTree(r.value - i.value));

        addOperation(Opcode.MUL, IntegerTree.class, RealTree.class, (i, r) -> new RealTree(i.value * r.value));
        addOperation(Opcode.MUL, RealTree.class, IntegerTree.class, (r, i) -> new RealTree(r.value * i.value));

        addOperation(Opcode.DIV, IntegerTree.class, RealTree.class, (i, r) -> new RealTree(i.value / r.value));
        addOperation(Opcode.DIV, RealTree.class, IntegerTree.class, (r, i) -> new RealTree(r.value / i.value));

        addOperation(Opcode.POW, IntegerTree.class, RealTree.class, (i, r) -> new RealTree(Math.pow(i.value, r.value)));
        addOperation(Opcode.POW, RealTree.class, IntegerTree.class, (r, i) -> new RealTree(Math.pow(r.value, i.value)));

        addOperation(Opcode.ADD, RealTree.class, RealTree.class, (i1, i2) -> new RealTree(i1.value + i2.value));
        addOperation(Opcode.SUB, RealTree.class, RealTree.class, (i1, i2) -> new RealTree(i1.value - i2.value));
        addOperation(Opcode.MUL, RealTree.class, RealTree.class, (i1, i2) -> new RealTree(i1.value * i2.value));
        addOperation(Opcode.DIV, RealTree.class, RealTree.class, (i1, i2) -> new RealTree((double) i1.value / i2.value));
        addOperation(Opcode.MOD, RealTree.class, RealTree.class, (i1, i2) -> new RealTree(i1.value % i2.value));
        addOperation(Opcode.POW, RealTree.class, RealTree.class, (i1, i2) -> new RealTree((int) Math.pow(i1.value, i2.value)));
    }

    private Map<Integer, Tree> locals = new HashMap<>();

    private void forgetValues(){
        locals.replaceAll((i, t) -> null);
    }


    @Override
    public Void visitUnaryOperationTree(UnaryOperationTree operationTree, Tree parent) {
        scan(operationTree.exp, operationTree);

        switch (operationTree.operation){
            case NOT -> {
                if (operationTree.exp instanceof BooleanTree b){
                    b.value = !b.value;
                    parent.replace(operationTree, operationTree.exp);
                    optimizationPerformed();
                }
                else if (operationTree.exp instanceof IntegerTree i){
                    i.value = ~i.value;
                    parent.replace(operationTree, operationTree.exp);
                    optimizationPerformed();
                }
            }
            case NEG -> {
                if (operationTree.exp instanceof IntegerTree i){
                    i.value = -i.value;
                    parent.replace(operationTree, operationTree.exp);
                    optimizationPerformed();
                }
            }
            case POS -> {
                if (operationTree.exp instanceof IntegerTree){
                    parent.replace(operationTree, operationTree.exp);
                    optimizationPerformed();
                }
            }
        }

        return null;
    }

    @Override
    public Void visitBinaryOperationTree(BinaryOperationTree operationTree, Tree parent) {

        scan(operationTree.left, operationTree);
        scan(operationTree.right, operationTree);

        if (operationTree.left instanceof StringTree s && operationTree.right instanceof LiteralTree<?> l){
            StringTree stringTree = new StringTree(s.value + l.value);
            parent.replace(operationTree, stringTree);
            return null;
        }
        else if (operationTree.right instanceof StringTree s && operationTree.left instanceof LiteralTree<?> l){
            StringTree stringTree = new StringTree(l.value + s.value);
            parent.replace(operationTree, stringTree);
            return null;
        }

        HashMap<Class<? extends Tree>, Operation> map = operationTable.get(operationTree.operation).get(operationTree.left.getClass());
        if (map != null){
            Operation operation = map.get(operationTree.right.getClass());
            if (operation != null){
                Tree folded = operation.operate(operationTree.left, operationTree.right);
                parent.replace(operationTree, folded);
                optimizationPerformed();
            }
        }

        return null;
    }

    @Override
    public Void visitIfElseTree(IfElseTree ifElseTree, Tree parent) {
        scan(ifElseTree.condition, ifElseTree);

        Map<Integer, Tree> rememberedLocals = new HashMap<>(locals);
        scan(ifElseTree.ifBody, ifElseTree);

        locals = rememberedLocals;
        scan(ifElseTree.ifBody, ifElseTree);
        forgetValues();

        return null;
    }

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Tree parent) {
        scan(forLoopTree.iterable, forLoopTree);
        forgetValues();
        scan(forLoopTree.body, forLoopTree);
        forgetValues();
        return null;
    }

    @Override
    public Void visitLoadLocalTree(LoadLocalTree loadLocalTree, Tree parent) {
        int address = loadLocalTree.address();
        if (!locals.containsKey(address))
            locals.put(address, null);
        Tree value = locals.get(address);
        if (value != null) {
            parent.replace(loadLocalTree, value);
            optimizationPerformed();
        }
        return null;
    }

    @Override
    public Void visitStoreLocalTree(StoreLocalTree storeLocalTree, Tree parent) {
        scan(storeLocalTree.child, storeLocalTree);
        int address = storeLocalTree.address;
        locals.put(address, storeLocalTree.child);
        return null;
    }

    @Override
    public Void visitReadContainerTree(ReadContainerTree readTree, Tree parent) {
        scan(readTree.key, readTree);

        if (readTree.container instanceof ArrayTree a
                && readTree.key instanceof IntegerTree i){
            parent.replace(readTree, a.arguments().get(i.value));
            optimizationPerformed();
        }
        else if (readTree.container instanceof StringTree s
                && readTree.key instanceof IntegerTree i){
            String subString = Character.toString(s.value.charAt(i.value));
            parent.replace(readTree, new StringTree(subString));
            optimizationPerformed();
        }
        else if (readTree.container instanceof RangeTree r
                && readTree.key instanceof IntegerTree i){
            if (r.from instanceof IntegerTree f && r.to instanceof IntegerTree t){
                if (i.value >= f.value && i.value < t.value){
                    parent.replace(readTree, new IntegerTree(i.value));
                    optimizationPerformed();
                }
            }
        }

        return null;
    }

}
