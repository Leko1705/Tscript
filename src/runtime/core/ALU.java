package runtime.core;

import runtime.type.*;
import tscriptc.generation.Opcode;

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
        addOperation(Opcode.ADD, TInteger.class, TInteger.class, (i1, i2) ->new TInteger(i1.get() + i2.get()));
        addOperation(Opcode.SUB, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.get() - i2.get()));
        addOperation(Opcode.MUL, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.get() * i2.get()));
        addOperation(Opcode.DIV, TInteger.class, TInteger.class, (i1, i2) -> new TReal((double) i1.get() / i2.get()));
        addOperation(Opcode.IDIV, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.get() / i2.get()));
        addOperation(Opcode.MOD, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.get() % i2.get()));
        addOperation(Opcode.POW, TInteger.class, TInteger.class, (i1, i2) -> new TInteger((int) Math.pow(i1.get(), i2.get())));

        addOperation(Opcode.SLA, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.get() << i2.get()));
        addOperation(Opcode.SRA, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.get() >> i2.get()));
        addOperation(Opcode.SRL, TInteger.class, TInteger.class, (i1, i2) -> new TInteger(i1.get() >>> i2.get()));

        addOperation(Opcode.LT, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() < i2.get()));
        addOperation(Opcode.LT, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() < i2.get()));
        addOperation(Opcode.LT, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() < i2.get()));
        addOperation(Opcode.LT, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() < i2.get()));

        addOperation(Opcode.LEQ, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() <= i2.get()));
        addOperation(Opcode.LEQ, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() <= i2.get()));
        addOperation(Opcode.LEQ, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() <= i2.get()));
        addOperation(Opcode.LEQ, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() <= i2.get()));

        addOperation(Opcode.GT, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() > i2.get()));
        addOperation(Opcode.GT, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() > i2.get()));
        addOperation(Opcode.GT, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() > i2.get()));
        addOperation(Opcode.GT, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() > i2.get()));

        addOperation(Opcode.GEQ, TInteger.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() >= i2.get()));
        addOperation(Opcode.GEQ, TInteger.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() >= i2.get()));
        addOperation(Opcode.GEQ, TReal.class, TInteger.class, (i1, i2) -> TBoolean.of(i1.get() >= i2.get()));
        addOperation(Opcode.GEQ, TReal.class, TReal.class, (i1, i2) -> TBoolean.of(i1.get() >= i2.get()));

        addOperation(Opcode.AND, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.get() & i2.get()));
        addOperation(Opcode.OR, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.get() | i2.get()));
        addOperation(Opcode.XOR, TInteger.class, TInteger.class, (i1, i2) -> new TInteger( i1.get() ^ i2.get()));

        addOperation(Opcode.AND, TBoolean.class, TBoolean.class, (i1, i2) -> TBoolean.of(i1.get() && i2.get()));
        addOperation(Opcode.OR, TBoolean.class, TBoolean.class, (i1, i2) -> TBoolean.of(i1.get() || i2.get()));
        addOperation(Opcode.XOR, TBoolean.class, TBoolean.class, (i1, i2) -> TBoolean.of(i1.get() ^ i2.get()));

        addOperation(Opcode.ADD, TInteger.class, TReal.class, (i, r) -> new TReal(i.get() + r.get()));
        addOperation(Opcode.ADD, TReal.class, TInteger.class, (r, i) -> new TReal(r.get() + i.get()));

        addOperation(Opcode.SUB, TInteger.class, TReal.class, (i, r) -> new TReal(i.get() - r.get()));
        addOperation(Opcode.SUB, TReal.class, TInteger.class, (r, i) -> new TReal(r.get() - i.get()));

        addOperation(Opcode.MUL, TInteger.class, TReal.class, (i, r) -> new TReal(i.get() * r.get()));
        addOperation(Opcode.MUL, TReal.class, TInteger.class, (r, i) -> new TReal(r.get() * i.get()));

        addOperation(Opcode.DIV, TInteger.class, TReal.class, (i, r) -> new TReal(i.get() / r.get()));
        addOperation(Opcode.DIV, TReal.class, TInteger.class, (r, i) -> new TReal(r.get() / i.get()));

        addOperation(Opcode.POW, TInteger.class, TReal.class, (i, r) -> new TReal(Math.pow(i.get(), r.get())));
        addOperation(Opcode.POW, TReal.class, TInteger.class, (r, i) -> new TReal(Math.pow(r.get(), i.get())));

        addOperation(Opcode.ADD, TReal.class, TReal.class, (i1, i2) ->new TReal(i1.get() + i2.get()));
        addOperation(Opcode.SUB, TReal.class, TReal.class, (i1, i2) -> new TReal(i1.get() - i2.get()));
        addOperation(Opcode.MUL, TReal.class, TReal.class, (i1, i2) -> new TReal(i1.get() * i2.get()));
        addOperation(Opcode.DIV, TReal.class, TReal.class, (i1, i2) -> new TReal((double) i1.get() / i2.get()));
        addOperation(Opcode.MOD, TReal.class, TReal.class, (i1, i2) -> new TReal(i1.get() % i2.get()));
        addOperation(Opcode.POW, TReal.class, TReal.class, (i1, i2) -> new TReal(Math.pow(i1.get(), i2.get())));
    }

    public static Data performBinaryOperation(Data f, Data s, Opcode operation, TThread context){
        TObject first = context.unpack(f);
        TObject second = context.unpack(s);

        if (operation == Opcode.ADD && (first instanceof TString || second instanceof TString))
            return new TString(first.toString() + second);
        Operation algorithm = getOperation(first.getClass(), second.getClass(), operation);
        if (algorithm == null){
            context.reportRuntimeError("can not handle <" + first + "> and <" + second + ">");
            return null;
        }
        return algorithm.operate(first, second);
    }

    public static Data performUnaryOperation(Data value, Opcode operation, TThread context){
        TObject object = context.unpack(value);
        if (operation == Opcode.NOT)
            return performNotOp(object, context);
        else if (operation == Opcode.NEG)
            return performNegationOp(object, context);
        else if (operation == Opcode.POS)
            return checkIfIsInt(object, context);
        throw new UnsupportedOperationException("" + operation);
    }

    private static Data checkIfIsInt(TObject value, TThread context) {
        if (!(value instanceof TInteger) && !(value instanceof TReal)){
            context.reportRuntimeError("can not use '+' prefix on <" + value + ">");
            return null;
        }
        return value;
    }

    private static Data performNotOp(TObject object, TThread context){
        if (object instanceof TBoolean b)
            return TBoolean.of(!b.get());
        else if (object instanceof TInteger i)
            return new TInteger(~i.get());
        context.reportRuntimeError("can not perform 'not' operation on <" + object + ">");
        return null;
    }

    private static Data performNegationOp(TObject object, TThread context){
        if (object instanceof TInteger i)
            return new TInteger(-i.get());
        else if (object instanceof TReal i)
            return new TReal(-i.get());
        context.reportRuntimeError("can not perform 'not' operation on <" + object + ">");
        return null;
    }

    private static Operation getOperation(Class<? extends TObject> first, Class<? extends TObject> second, Opcode operation){
        HashMap<Class<? extends TObject>, HashMap<Class<? extends TObject>, Operation>> operationAssociations = operationTable.get(operation);
        if (operationAssociations == null) return null;
        HashMap<Class<? extends TObject>, Operation> opsPerType = operationAssociations.get(first);
        if (opsPerType == null) return null;
        else return opsPerType.get(second);
    }

    private interface Operation<L, R> {
        Data operate(L left, R right);
    }

}