package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.utils.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Range extends PrimitiveObject<Tuple<TInteger, TInteger>> implements ContainerAccessibleObject, IterableObject {

    public static final Type TYPE =
            new Type.Builder("Range")
                    .setParameters(Parameters.newInstance()
                            .add("begin", null)
                            .add("end", null))
                    .setConstructor((thread, params) -> {
                        TObject from = params.get(0);
                        TObject to = params.get(1);

                        int fromVal, toVal;

                        if (from instanceof TInteger integer) {
                            fromVal = integer.getValue();
                        }
                        else if (from instanceof TReal real){
                            fromVal = real.getValue().intValue();
                        }
                        else {
                            thread.reportRuntimeError("Integer expected; got: " + from.getType());
                            return null;
                        }

                        if (to instanceof TInteger integer) {
                            toVal = integer.getValue();
                        }
                        else if (to instanceof TReal real){
                            toVal = real.getValue().intValue();
                        }
                        else {
                            thread.reportRuntimeError("Integer expected, got; " + from.getType());
                            return null;
                        }

                        return new Range(fromVal, toVal);
                    }).build();

    private final Map<String, Member> methods;

    public Range(int start, int end) {
        this(new TInteger(start), new TInteger(end));
    }

    public Range(TInteger from, TInteger to) {
        super(new Tuple<>(from, to));
        methods = new HashMap<>(Map.of(
                "begin", new Member(Visibility.PUBLIC, false, "begin", new BeginMethod()),
                "end", new Member(Visibility.PUBLIC, false, "end", new EndMethod()),
                "size", new Member(Visibility.PUBLIC, false, "size", new SizeMethod())
        ));
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
    public Iterable<Member> getMembers() {
        return methods.values();
    }

    @Override
    public Member loadMember(String name) {
        return methods.get(name);
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

    @Override
    public String getDisplayName() {
        return getFrom() + ":" + getTo();
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

    private class BeginMethod extends NativeFunction {

        @Override
        public String getName() {
            return "begin";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return getValue().getFirst();
        }
    }

    private class EndMethod extends NativeFunction {

        @Override
        public String getName() {
            return "end";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return getValue().getSecond();
        }
    }

    private class SizeMethod extends NativeFunction {

        @Override
        public String getName() {
            return "size";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return new TReal((double)getTo() - (double)getFrom());
        }
    }
}
