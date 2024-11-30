package com.tscript.runtime.utils;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public final class FastMergedList<T> extends AbstractList<T> implements ImmutableList<T> {

    private final List<T> first, second;

    public FastMergedList(List<T> first, List<T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public T get(int index) {
        if (index < first.size())
            return first.get(index);
        return second.get(index - first.size());
    }

    @Override
    public int size() {
        return first.size() + second.size();
    }

    @Override
    public String toString() {
        return "FastMergedList{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
