package com.tscript.runtime.jit.comp.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class State<T> implements Iterable<T> {

    private final Deque<T> stack = new ArrayDeque<>();

    private T current;

    public State(T initial) {
        this.current = initial;
    }

    public T get(){
        return current;
    }

    public void set(T value) {
        this.current = value;
    }

    public void storeAndSet(T value) {
        stack.push(current);
        current = value;
    }

    public void restore(){
        current = stack.remove();
    }

    public T restoreAndGet() {
        restore();
        return current;
    }

    public T getAndRestore() {
        T result = current;
        restore();
        return result;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return stack.iterator();
    }
}
