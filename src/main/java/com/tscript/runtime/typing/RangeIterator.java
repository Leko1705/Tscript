package com.tscript.runtime.typing;

import com.tscript.runtime.jit.JITSensitive;

import java.util.List;

/**
 * Iterates over the integers of a {@link Range}. Its state is public so that compiled code can
 * iterate without allocating an Integer object per step.
 */
public final class RangeIterator implements IteratorObject {

    public static final Type TYPE = Type.newBuilder("RangeIterator")
            .setConstructor((env, params) -> { throw new AssertionError(); })
            .setAbstract(true)
            .build();

    /** the next value */
    @JITSensitive public int current;

    /** the exclusive upper bound */
    @JITSensitive public final int end;

    @JITSensitive
    public RangeIterator(int from, int end) {
        this.current = from;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return current < end;
    }

    @Override
    public TObject next() {
        return new TInteger(current++);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public Iterable<Member> getMembers() {
        return List.of();
    }
}
