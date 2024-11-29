package com.tscript.runtime.core;

import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class Frame {

    public static Frame createNativeFrame(Callable f){
        return new Frame(null, f.getName(), null, 0, 0, null);
    }
    

    private final TObject owner;
    private final String name;
    private final byte[][] instructions;

    private int ip = 0, sp = 0;

    public final TObject[] stack;
    public final TObject[] locals;

    private final Module module;

    private int line = -1;

    private final ArrayDeque<Integer> safeSpots = new ArrayDeque<>();

    private final Map<String, TObject> names = new HashMap<>();

    public Frame(TObject owner, String name, byte[][] instructions, int stackSize, int locals, Module module) {
        this.owner = owner;
        this.name = name;
        this.instructions = instructions;
        this.stack = new TObject[stackSize];
        this.locals = new TObject[locals];
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public TObject getOwner() {
        return owner;
    }

    public byte[] fetch() {
        return instructions[ip++];
    }

    public void push(TObject data) {
        stack[sp++] = data;
    }

    public TObject pop() {
        return stack[--sp];
    }

    public void jumpTo(int address) {
        ip = address;
    }

    public void store(int index, TObject data) {
        locals[index] = data;
    }

    public TObject load(int index) {
        return locals[index];
    }

    public TObject loadName(String name) {
        return names.get(name);
    }

    public boolean storeName(String name, TObject data) {
        if (names.containsKey(name))
            return false;
        names.put(name, data);
        return true;
    }

    public Module getModule() {
        return module;
    }

    public int line() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean inUnsafeSpot() {
        return !safeSpots.isEmpty();
    }

    public void enterUnsafeSpot(int fallBackAddress) {
        safeSpots.push(fallBackAddress);
    }

    public void leaveUnsafeSpot() {
        safeSpots.pop();
    }

    public void escapeError() {
        ip = safeSpots.pop();
        sp = 0;
    }

    @Override
    public String toString() {
        return name;
    }

}