package runtime.type;

import runtime.core.Data;

import java.util.Iterator;
import java.util.List;

public interface IteratorObject extends TObject, Iterator<Data> {


    boolean hasNext();


    Data next();


    static IteratorObject of(Iterator<Data> itr){
        return new BasicIterator(itr);
    }

    class BasicIterator implements IteratorObject {
        private static final TType type = new TType("Iterator", null);
        private final Iterator<Data> itr;
        public BasicIterator(Iterator<Data> itr) {
            this.itr = itr;
        }
        public boolean hasNext() {
            return itr.hasNext();
        }
        public Data next() {
            return itr.next();
        }
        public TType getType() {
            return type;
        }
        public Member get(int index) {
            throw new UnsupportedOperationException("get");
        }
        public int getIndex(String key) {
            return -1;
        }
        public Iterable<Member> getMembers() {
            return List.of();
        }
    }

}
