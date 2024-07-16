package com.tscript.runtime.core;

import com.tscript.runtime.debug.DataInfo;
import com.tscript.runtime.debug.Debuggable;
import com.tscript.runtime.debug.FrameInfo;
import com.tscript.runtime.heap.Heap;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.type.Callable;

import java.util.*;

public class Frame implements Debuggable<FrameInfo> {

    public static Frame createFakeFrame(Callable callable){
       return new Frame(null, callable.getOwner(), callable.getName(), null, 0, 0);
    }

    private final Data owner;
    private final String name;
    private final byte[][] instructions;

    private int ip = 0, sp = 0;

    protected final Data[] stack;
    protected final Data[] locals;

    private final Reference moduleRef;

    private int line = -1;

    private final ArrayDeque<Integer> safeAddresses = new ArrayDeque<>();

    private final Map<String, Data> names = new HashMap<>();

    public Frame(Reference moduleReference, Data owner, String name, byte[][] instructions, int stackSize, int locals) {
        this.moduleRef = moduleReference;
        this.owner = owner;
        this.name = name;
        this.instructions = instructions;
        this.stack = new Data[stackSize];
        this.locals = new Data[locals];
    }

    public String getName() {
        return name;
    }

    public Data getOwner() {
        return owner;
    }

    public byte[] fetch(){
        return instructions[ip++];
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

    public Data loadName(String name){
        return names.get(name);
    }

    public boolean storeName(String name, Data data){
        if (names.containsKey(name))
            return false;
        names.put(name, data);
        return true;
    }

    public Reference getModuleReference() {
        return moduleRef;
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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public FrameInfo loadInfo(Heap heap) {
        return new FrameInfoImpl(heap);
    }




    private class FrameInfoImpl implements FrameInfo {

        private final List<DataInfo> locals;
        private final List<DataInfo> stack;

        FrameInfoImpl(Heap heap){
            stack = listOf(Frame.this.stack, heap);
            locals = listOf(Frame.this.locals, heap);
        }

        @Override
        public String getName() {
            return name;
        }
        @Override
        public List<DataInfo> getStack() {
            return stack;
        }

        @Override
        public List<DataInfo> getLocals() {
            return locals;
        }

        private List<DataInfo> listOf(Data[] data, Heap heap){
            List<DataInfo> list = new ArrayList<>(data.length);
            for (Data value : data){
                list.add(value != null ? value.loadInfo(heap) : null);
            }
            return list;
        }

    }

}
