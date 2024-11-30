package com.tscript.runtime.utils;

public class Tuple<E1, E2> {

    private E1 e1;
    private E2 e2;

    public Tuple(E1 e1, E2 e2){
        this.e1 = e1;
        this.e2 = e2;
    }

    public E1 getFirst() {
        return e1;
    }

    public void setFirst(E1 e1) {
        this.e1 = e1;
    }

    public E2 getSecond() {
        return e2;
    }

    public void setSecond(E2 e2) {
        this.e2 = e2;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Tuple<?,?> t && e1.equals(t.e1) && e2.equals(t.e2);
    }
}
