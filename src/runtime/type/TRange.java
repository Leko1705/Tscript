package runtime.type;

import runtime.core.Data;
import runtime.core.TThread;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TRange extends PrimitiveObject<Tuple<TInteger, TInteger>>
        implements ContainerAccessible, IterableObject {

    private static final TType type = new TType("Range", null);

    public TRange(TInteger from, TInteger to) {
        super(new Tuple<>(from, to));
    }

    public TRange(int from, int to){
        this(new TInteger(from), new TInteger(to));
    }

    @Override
    public Data readFromContainer(TThread thread, Data key) {
        TObject o = thread.unpack(key);
        if (!(o instanceof TInteger || o instanceof TRange)){
            thread.reportRuntimeError("invalid key " + o.getType() + ": <Integer> or <Range> expected");
            return null;
        }
        if (o instanceof TInteger i) {
            int index = i.get();
            if (index < getFrom() || index >= getTo()) {
                thread.reportRuntimeError("index " + index + " out of bounds for range " + this);
                return null;
            }
            return key;
        }
        else {
            TRange range = (TRange) o;
            int from = range.getFrom();
            if (from < getFrom()) from = 0;
            int to = range.getTo();
            if (to > getTo()) to = getTo();
            return new TRange(from, to);
        }
    }


    @Override
    public IteratorObject iterator() {
        return new TRangeIterator();
    }

    @Override
    public TType getType() {
        return type;
    }

    public int getFrom(){
        return get().getFirst().get();
    }

    public int getTo(){
        return get().getSecond().get();
    }

    @Override
    public String toString() {
        return getFrom() + ":" + getTo();
    }

    private class TRangeIterator implements IteratorObject {

        private static final TType type = new TType("RangeIterator", null);

        private int curr = TRange.this.get().getFirst().get();
        private final int border = TRange.this.get().getSecond().get();

        @Override
        public boolean hasNext() {
            return curr < border;
        }

        @Override
        public Data next() {
            return new TInteger(curr++);
        }

        @Override
        public TType getType() {
            return type;
        }

        @Override
        public Member get(int index) {
            throw new UnsupportedOperationException("get");
        }

        @Override
        public int getIndex(String key) {
            return -1;
        }

        @Override
        public Iterable<Member> getMembers() {
            return List.of();
        }

    }
}
