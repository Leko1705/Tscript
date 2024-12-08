package com.tscript.runtime.jit.optim;

import java.util.ArrayDeque;
import java.util.Deque;

public class State<T> {

    private final Deque<T> stack = new ArrayDeque<>();

    private T current;

    public State(T initial) {
        this.current = initial;
    }

    public T get(){
        return current;
    }

    public void storeAndSet(T value) {
        stack.push(current);
        current = value;
    }

    public void restore(){
        current = stack.remove();
    }

}
