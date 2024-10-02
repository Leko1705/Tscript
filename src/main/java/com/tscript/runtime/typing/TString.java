package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.TNIUtils;

import java.util.List;


public class TString extends PrimitiveObject<String>
        implements ContainerAccessibleObject, IterableObject{

    public static final Type TYPE =
            new Type.Builder("String")
                    .setConstructor((thread, params) -> new TString(TNIUtils.toString(thread, params.get(0))))
                    .build();

    public static final Type ITR_TYPE = new Type.Builder("StringIterator")
            .setConstructor((thread, params) ->  Null.INSTANCE)
            .setAbstract(true)
            .build();


    public TString(String value) {
        super(value);
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
        String content = getValue();
        if (key instanceof TInteger i) {
            int index = i.getValue();
            if (index < 0 || index >= content.length()) {
                thread.reportRuntimeError("index " + index + " out of bounds for length " + content.length());
                return null;
            }
            return new TString(Character.toString(content.charAt(index)));
        }
        else {
            Range range = (Range) key;
            int from = range.getFrom();
            if (from < 0) from = 0;
            int to = range.getTo();
            if (to > content.length()) to = content.length();
            String subString = content.substring(from, to);
            return new TString(subString);
        }
    }

    @Override
    public IteratorObject iterator() {
        return new TStringIterator();
    }

    private class TStringIterator implements IteratorObject {

        int i = 0;
        int len = TString.this.getValue().length();

        @Override
        public boolean hasNext() {
            return i < len;
        }

        @Override
        public TObject next() {
            char c = TString.this.getValue().charAt(i++);
            return new TString(Character.toString(c));
        }

        @Override
        public Type getType() {
            return ITR_TYPE;
        }

        @Override
        public Iterable<Member> getMembers() {
            return List.of();
        }

    }
}
