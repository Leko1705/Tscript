package com.tscript.runtime.core;


import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;

import java.util.HashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ALU {

    private static final HashMap<Opcode, HashMap<Class<? extends TObject>, HashMap<Class<? extends TObject>, Operation>>> operationTable = new HashMap<>();

    private static <L extends TObject, R extends TObject> void addOperation(Opcode opcode, Class<L> first, Class<R> second, Operation<L, R> operation){
        HashMap<Class<? extends TObject>, HashMap<Class<? extends TObject>, Operation>> operationAssociations;

        if (operationTable.containsKey(opcode)){
            operationAssociations = operationTable.get(opcode);
        }
        else {
            operationAssociations = new HashMap<>();
            operationTable.put(opcode, operationAssociations);
        }

        if (operationAssociations.containsKey(first)){
            HashMap<Class<? extends TObject>, Operation> operationsPerType = operationAssociations.get(first);
            operationsPerType.put(second, operation);
        }
        else {
            HashMap<Class<? extends TObject>, Operation> operationsPerType = new HashMap<>();
            operationsPerType.put(second, operation);
            operationAssociations.put(first, operationsPerType);
        }

    }


    static {
        init();
    }

    private static void init(){
        addOperation(Opcode.ADD, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.getValue() + i2.getValue()));
        addOperation(Opcode.ADD, TInteger.class, TReal.class, (i1, i2) -> new TReal(i1.getValue() + i2.getValue()));
        addOperation(Opcode.ADD, TReal.class, TInteger.class, (i1, i2) -> new TReal(i1.getValue() + i2.getValue()));
        addOperation(Opcode.ADD, TReal.class, TReal.class, (i1, i2) -> new TReal(i1.getValue() + i2.getValue()));

        addOperation(Opcode.SUB, TInteger.class, TInteger.class, (i1, i2) ->new TInteger(i1.getValue() - i2.getValue()));
        addOperation(Opcode.SUB, TInteger.class, TReal.class, (i1, i2) ->new TReal(i1.getValue() - i2.getValue()));
        addOperation(Opcode.SUB, TReal.class, TInteger.class, (i1, i2) ->new TReal(i1.getValue() - i2.getValue()));
        addOperation(Opcode.SUB, TReal.class, TReal.class, (i1, i2) ->new TReal(i1.getValue() - i2.getValue()));

        addOperation(Opcode.MUL, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.getValue() * i2.getValue()));
        addOperation(Opcode.MUL, TReal.class, TInteger.class, (i1, i2) -> new TReal(i1.getValue() * i2.getValue()));
        addOperation(Opcode.MUL, TInteger.class, TReal.class, (i1, i2) -> new TReal(i1.getValue() * i2.getValue()));
        addOperation(Opcode.MUL, TReal.class, TReal.class, (i1, i2) -> new TReal(i1.getValue() * i2.getValue()));

        addOperation(Opcode.DIV, TInteger.class, TInteger.class, (i1, i2) -> new TReal((double) i1.getValue() / i2.getValue()));
        addOperation(Opcode.DIV, TReal.class, TInteger.class, (i1, i2) -> new TReal((double) i1.getValue() / i2.getValue()));
        addOperation(Opcode.DIV, TInteger.class, TReal.class, (i1, i2) -> new TReal((double) i1.getValue() / i2.getValue()));
        addOperation(Opcode.DIV, TReal.class, TReal.class, (i1, i2) -> new TReal((double) i1.getValue() / i2.getValue()));

        addOperation(Opcode.IDIV, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.getValue() / i2.getValue()));
        addOperation(Opcode.MOD, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.getValue() % i2.getValue()));

        addOperation(Opcode.POW, TInteger.class, TInteger.class, (i1, i2) -> new TInteger((int) Math.pow(i1.getValue(), i2.getValue())));
        addOperation(Opcode.POW, TReal.class, TInteger.class, (i1, i2) -> new TReal(Math.pow(i1.getValue(), i2.getValue())));
        addOperation(Opcode.POW, TInteger.class, TReal.class, (i1, i2) -> new TReal(Math.pow(i1.getValue(), i2.getValue())));
        addOperation(Opcode.POW, TReal.class, TReal.class, (i1, i2) -> new TReal(Math.pow(i1.getValue(), i2.getValue())));

        addOperation(Opcode.SLA, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.getValue() << i2.getValue()));
        addOperation(Opcode.SRA, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.getValue() >> i2.getValue()));
        addOperation(Opcode.SRL, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.getValue() >>> i2.getValue()));

        addOperation(Opcode.LT, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() < i2.getValue()));
        addOperation(Opcode.LT, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() < i2.getValue()));
        addOperation(Opcode.LT, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() < i2.getValue()));
        addOperation(Opcode.LT, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() < i2.getValue()));

        addOperation(Opcode.GT, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() > i2.getValue()));
        addOperation(Opcode.GT, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() > i2.getValue()));
        addOperation(Opcode.GT, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() > i2.getValue()));
        addOperation(Opcode.GT, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() > i2.getValue()));

        addOperation(Opcode.LEQ, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() <= i2.getValue()));
        addOperation(Opcode.LEQ, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() <= i2.getValue()));
        addOperation(Opcode.LEQ, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() <= i2.getValue()));
        addOperation(Opcode.LEQ, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() <= i2.getValue()));

        addOperation(Opcode.GEQ, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() >= i2.getValue()));
        addOperation(Opcode.GEQ, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() >= i2.getValue()));
        addOperation(Opcode.GEQ, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.getValue() >= i2.getValue()));
        addOperation(Opcode.GEQ, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.getValue() >= i2.getValue()));

        addOperation(Opcode.AND, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.getValue() & i2.getValue()));
        addOperation(Opcode.AND, TBoolean.class, TBoolean.class, (i1, i2) -> TBoolean.of(i1.getValue() && i2.getValue()));

        addOperation(Opcode.OR, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.getValue() | i2.getValue()));
        addOperation(Opcode.OR, TBoolean.class, TBoolean.class, (i1, i2) -> TBoolean.of(i1.getValue() || i2.getValue()));

        addOperation(Opcode.XOR, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.getValue() ^ i2.getValue()));
        addOperation(Opcode.XOR, TBoolean.class, TBoolean.class, (i1, i2) -> TBoolean.of(i1.getValue() ^ i2.getValue()));
    }

    public static TObject performBinaryOperation(TObject first, TObject second, Opcode operation, Environment env){
        if (operation == Opcode.ADD && (first instanceof TString || second instanceof TString))
            return new TString(TNIUtils.toString(env, first) + TNIUtils.toString(env, second));
        Operation algorithm = getValueOperation(first.getClass(), second.getClass(), operation);
        if (algorithm == null) return null;
        return algorithm.operate(first, second);
    }

    public static TObject performUnaryOperation(TObject object, Opcode operation){
        if (operation == Opcode.NOT)
            return performNotOp(object);
        else if (operation == Opcode.NEG)
            return performNegationOp(object);
        else if (operation == Opcode.POS)
            return checkIsInt(object);
        throw new UnsupportedOperationException(operation.name());
    }

    private static TObject checkIsInt(TObject value) {
        if (!(value instanceof TInteger) && !(value instanceof TReal)){
            return null;
        }
        return value;
    }

    private static TObject performNotOp(TObject object){
        if (object instanceof TBoolean b)
            return TBoolean.of(!b.getValue());
        else if (object instanceof TInteger i)
            return new TInteger(~i.getValue());
        return null;
    }

    private static TObject performNegationOp(TObject object){
        if (object instanceof TInteger i)
            return new TInteger(-i.getValue());
        else if (object instanceof TReal i)
            return new TReal(-i.getValue());
        return null;
    }

    private static Operation getValueOperation(Class<? extends TObject> first, Class<? extends TObject> second, Opcode operation){
        HashMap<Class<? extends TObject>, HashMap<Class<? extends TObject>, Operation>> operationAssociations = operationTable.get(operation);
        if (operationAssociations == null) return null;
        HashMap<Class<? extends TObject>, Operation> opsPerType = operationAssociations.get(first);
        if (opsPerType == null) return null;
        else return opsPerType.get(second);
    }

    private interface Operation<L, R> {
        TObject operate(L left, R right);
    }

}