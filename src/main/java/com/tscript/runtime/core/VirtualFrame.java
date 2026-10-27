package com.tscript.runtime.core;

import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.TObject;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VirtualFrame implements Frame {

    private final VirtualFunction.VirtualMetadata metadata;
    private final TObject owner;

    public int ip = 0, sp = 0;

    public final TObject[] stack;
    public final TObject[] locals;

    private int line = -1;

    /**
     * true if the return value of this frame is not pushed onto the stack of the calling frame
     * (frames of super constructors materialized by the JIT: CALL_SUPER leaves nothing on the stack)
     */
    public boolean discardsResult = false;
    public final ArrayDeque<Integer> safeSpots = new ArrayDeque<>();

    public final Map<String, TObject> names = new HashMap<>();


    public VirtualFrame(VirtualFunction f){
        this(f.getMetadata(), f.getOwner());
    }

    public VirtualFrame(VirtualFunction.VirtualMetadata metadata, TObject owner) {
        this.metadata = Objects.requireNonNull(metadata);
        this.owner = Objects.requireNonNull(owner);
        this.stack = new TObject[metadata.stackSize()];
        this.locals = new TObject[metadata.locals()];
    }

    public VirtualFrame(TObject owner, VirtualFunction.VirtualMetadata metadata, TObject[] stack, TObject[] locals) {
        this.metadata = Objects.requireNonNull(metadata);
        this.owner = Objects.requireNonNull(owner);
        this.stack = stack;
        this.locals = locals;
    }

    public byte[] fetch() {
        byte[][] ins = metadata.instructions();
        return ins[ip++];
    }

    public String getName() {
        return metadata.name();
    }

    @Override
    public TObject getOwner() {
        return owner;
    }

    @Override
    public VirtualFunction.VirtualMetadata getMetadata() {
        return metadata;
    }

    public void push(TObject data) {
        stack[sp++] = Objects.requireNonNull(data);
    }

    public TObject pop() {
        TObject top = stack[--sp];
        stack[sp] = null;
        return top;
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
        return metadata.module();
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
        return getName();
    }

    public byte[][] getInstructions() {
        return metadata.instructions();
    }
}