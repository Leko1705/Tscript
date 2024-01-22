package runtime.type;

import runtime.core.Data;
import runtime.core.TThread;

import java.util.ArrayList;
import java.util.List;

public class TString extends PrimitiveObject<String>
        implements ContainerAccessible, IterableObject {

    private static final TType type = new TType("String", null);

    public TString(String value) {
        super(value);
    }

    @Override
    public Data readFromContainer(TThread thread, Data key) {
        TObject o = thread.unpack(key);
        if (!(o instanceof TInteger || o instanceof TRange)){
            thread.reportRuntimeError("invalid key " + o.getType() + ": <Integer> or <Range> expected");
            return null;
        }
        String content = get();
        if (o instanceof TInteger i) {
            int index = i.get();
            if (index < 0 || index >= content.length()) {
                thread.reportRuntimeError("index " + index + " out of bounds for length " + content.length());
                return null;
            }
            return new TString(Character.toString(content.charAt(index)));
        }
        else {
            TRange range = (TRange) o;
            int from = range.get().getFirst().get();
            if (from < 0) from = 0;
            int to = range.get().getSecond().get();
            if (to > content.length()) to = content.length();
            String subString = content.substring(from, to);
            return new TString(subString);
        }
    }

    @Override
    public IteratorObject iterator() {
        return new TStringIterator();
    }

    @Override
    public TType getType() {
        return type;
    }

    private class TStringIterator implements IteratorObject {

        private static final TType type = new TType("StringIterator", null);

        int i = 0;

        @Override
        public boolean hasNext() {
            return i < TString.this.get().length();
        }

        @Override
        public Data next() {
            char c = TString.this.get().charAt(i++);
            return new TString(Character.toString(c));
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
