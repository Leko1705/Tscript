package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;

import java.util.*;

class PoolBuilder implements Pool {

    public final Entry<?>[] entries;

    protected PoolBuilder(int size) {
        this.entries = new Entry[size];
    }

    @Override
    public String loadName(byte b1, byte b2) {
        return (String) load(b1, b2);
    }

    @Override
    public TObject loadConstant(byte b1, byte b2) {
        return (TObject) load(b1, b2);
    }

    private Object load(byte b1, byte b2) {
        return entries[Conversion.from2Bytes(b1, b2)].load(this);
    }

    public void put(int index, Entry<?> entry) {
        entries[index] = entry;
    }


    protected interface Entry<T> {
        T load(Pool pool);
    }

    private static class ConstantEntry<T> implements Entry<T> {

        private final T value;

        private ConstantEntry(T value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public T load(Pool pool) {
            return value;
        }
    }

    protected static class IntEntry extends ConstantEntry<TInteger> {
        IntEntry(int i) {
            super(new TInteger(i));
        }
    }

    protected static class StringEntry extends ConstantEntry<TString> {
        StringEntry(String value) {
            super(new TString(value));
        }
    }

    protected static class BooleanEntry extends ConstantEntry<TBoolean> {
        BooleanEntry(TBoolean value) {
            super(value);
        }
    }

    protected static class RealEntry extends ConstantEntry<TReal> {
        RealEntry(double value) {
            super(new TReal(value));
        }
    }

    protected static class NullEntry implements Entry<Null> {
        @Override
        public Null load(Pool pool) {
            return Null.INSTANCE;
        }
    }

    protected static class RangeEntry implements Entry<Range> {

        private final byte b1, b2, b3, b4;

        protected RangeEntry(byte b1, byte b2, byte b3, byte b4) {
            this.b1 = b1;
            this.b2 = b2;
            this.b3 = b3;
            this.b4 = b4;
        }

        @Override
        public Range load(Pool pool) {
            return new Range((TInteger) pool.loadConstant(b1, b2), (TInteger) pool.loadConstant(b3, b4));
        }
    }


    protected static class ArrayEntry implements Entry<TArray> {
        private final byte[] contentReferences;

        protected ArrayEntry(byte[] contentReferences) {
            this.contentReferences = contentReferences;
        }

        @Override
        public TArray load(Pool pool) {
            List<TObject> content = new ArrayList<>();
            for (int i = 0; i < contentReferences.length; i += 2) {
                TObject element = pool.loadConstant(contentReferences[i], contentReferences[i + 1]);
                content.add(element);
            }
            return new TArray(content);
        }
    }


    protected static class DictionaryEntry implements Entry<TDictionary> {

        private final byte[] contentReferences;

        protected DictionaryEntry(byte[] contentReferences) {
            this.contentReferences = contentReferences;
        }

        @Override
        public TDictionary load(Pool pool) {
            Map<TObject, TObject> content = new LinkedHashMap<>();
            for (int i = 0; i < contentReferences.length; i += 4) {
                TObject key = pool.loadConstant(contentReferences[i], contentReferences[i + 1]);
                TObject value = pool.loadConstant(contentReferences[i + 2], contentReferences[i + 3]);
                content.put(key, value);
            }
            return new TDictionary(content);
        }
    }


    protected static class UTF8Entry extends ConstantEntry<String> {
        UTF8Entry(String value) {
            super(value);
        }
    }

}
