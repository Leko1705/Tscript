package runtime.core;

import runtime.jit.JITSensitive;
import runtime.type.Callable;

import java.util.ArrayDeque;

public class Frame {

    public static Frame createFakeFrame(Callable callable){
       return new Frame(callable.getOwner(), callable.getName(), null, 0, 0, null);
    }


    private final Data owner;
    private final String name;
    private final byte[][] instructions;

    private int ip = 0, sp = 0;

    private byte[] lastInstruction;

    private final Data[] stack;
    private final Data[] locals;

    private final Pool pool;

    private int line;

    private final ArrayDeque<Integer> safeAddresses = new ArrayDeque<>();

    public Frame(Data owner, String name, byte[][] instructions, int stackSize, int locals, Pool pool) {
        this.owner = owner;
        this.name = name;
        this.instructions = instructions;
        this.stack = new Data[stackSize];
        this.locals = new Data[locals];
        this.pool = pool;
    }

    public String getName() {
        return name;
    }

    public Data getOwner() {
        return owner;
    }

    public byte[] fetch(){
        return lastInstruction = instructions[ip++];
    }

    public void push(Data data){
        stack[sp++] = data;
    }

    public Data pop(){
        return stack[--sp];
    }

    public void jumpTo(int address){
        ip = address;
    }

    public Data store(int index, Data data){
        Data replaced = locals[index];
        locals[index] = data;
        return replaced;
    }

    public Data load(int index){
        return locals[index];
    }

    public Pool getPool() {
        return pool;
    }

    public int line() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean inSafeSpot() {
        return !safeAddresses.isEmpty();
    }

    public void enterSafeSpot(int safeAddress){
        safeAddresses.push(safeAddress);
    }

    public void leaveSafeSpot(){
        safeAddresses.pop();
    }

    public void escapeError() {
        ip = safeAddresses.pop();
        sp = 0;
    }

    @JITSensitive
    protected int getIp() {
        return ip;
    }
}
