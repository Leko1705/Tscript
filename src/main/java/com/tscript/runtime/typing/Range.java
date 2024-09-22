package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.utils.Tuple;

import java.util.List;


public class Range extends PrimitiveObject<Tuple<TInteger, TInteger>> implements ContainerAccessibleObject, IterableObject {

    public static final Type TYPE =
            new Type.Builder("Range").setConstructor((thread, params) -> Null.INSTANCE).build();

    public Range(int start, int end) {
        this(new TInteger(start), new TInteger(end));
    }

    public Range(TInteger from, TInteger to) {
        super(new Tuple<>(from, to));
    }

    public int getFrom(){
        return getValue().getFirst().getValue();
    }

    public int getTo(){
        return getValue().getSecond().getValue();
    }

    @Override
    public  Type getType() {
        return TYPE;
    }

    @Override
    public TObject readFromContainer(TThread thread, TObject key) {
        if (!(key instanceof TInteger || key instanceof Range)){
            thread.reportRuntimeError("invalid key " + key.getType() + ": <Integer> or <Range> expected");
            return null;
        }
        if (key instanceof TInteger i) {
            int index = i.getValue();
            if (index < getFrom() || index >= getTo()) {
                thread.reportRuntimeError("index " + index + " out of bounds for range " + this);
                return null;
            }
            return key;
        }
        else {
            Range range = (Range) key;
            int from = range.getFrom();
            if (from < getFrom()) from = 0;
            int to = range.getTo();
            if (to > getTo()) to = getTo();
            return new Range(from, to);
        }
    }

    @Override
    public IteratorObject iterator() {
        return new TRangeIterator();
    }

    private class TRangeIterator implements IteratorObject {

        private static final Type type = new Type.Builder("RangeIterator")
                .setConstructor((thread, params) -> Null.INSTANCE)
                .setAbstract(true).build();

        private int curr = Range.this.getFrom();
        private final int border = Range.this.getTo();

        @Override
        public boolean hasNext() {
            return curr < border;
        }

        @Override
        public TObject next() {
            return new TInteger(curr++);
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Iterable<Member> getMembers() {
            return List.of();
        }

    }
}
