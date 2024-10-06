package com.tscript.runtime.typing;


import com.tscript.runtime.core.ALU;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.utils.Tuple;

import java.util.*;

public class TArray extends PrimitiveObject<List<TObject>>
        implements ContainerWriteableObject, ContainerAccessibleObject, IterableObject {

    public static final Type TYPE =
            new Type.Builder("Array")
                    .setParameters(Parameters.newInstance()
                            .add("size_or_other", new TInteger(0))
                            .add("value", Null.INSTANCE))
                    .setConstructor((thread, params) -> {
                        TObject size_or_other = params.get(0);

                        if (size_or_other instanceof TInteger integer) {
                            int count = integer.getValue();
                            TObject other = params.get(1);
                            ArrayList<TObject> list = new ArrayList<>(count);
                            for (int i = 0; i < count; i++) list.add(other);
                            return new TArray(list);
                        }

                        else if (size_or_other instanceof TArray arr){
                            return new TArray(arr.getValue());
                        }

                        thread.reportRuntimeError("can not convert " + size_or_other.getType() + " to Array");
                        return null;
                    })
                    .build();

    public static final Type ITR_TYPE =
            new Type.Builder("ArrayIterator")
                    .setAbstract(true)
                    .addMember(new Member(Visibility.PUBLIC, false, "concat", new ConcatStaticMethod()))
                    .setConstructor((thread, params) -> Null.INSTANCE)
                    .build();

    private final Map<String, Member> methods;

    public TArray(List<TObject> value) {
        super(value);
        methods = new HashMap<>(Map.of(
                "push", new Member(Visibility.PUBLIC, false, "push", new PushMethod()),
                "pop", new Member(Visibility.PUBLIC, false, "pop", new PopMethod()),
                "size", new Member(Visibility.PUBLIC, false, "size", new SizeMethod()),
                "insert", new Member(Visibility.PUBLIC, false, "insert", new InsertMethod()),
                "remove", new Member(Visibility.PUBLIC, false, "remove", new RemoveMethod()),
                "keys", new Member(Visibility.PUBLIC, false, "keys", new KeysMethod()),
                "values", new Member(Visibility.PUBLIC, false, "values", new ValuesMethod()),
                "sort", new Member(Visibility.PUBLIC, false, "sort", new SortMethod())
        ));
    }

    public TArray(){
        this(new ArrayList<>());
    }

    public TArray(TObject... value) {
        this(new ArrayList<>(List.of(value)));
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
    public  Type getType() {
        return TYPE;
    }

    @Override
    public TObject readFromContainer(TThread thread, TObject key) {
        if (!(key instanceof TInteger || key instanceof Range)){
            thread.reportRuntimeError("invalid key " + key.getType() + ": <Integer> or <Range> expected");
            return null;
        }
        List<TObject> content = getValue();
        if (key instanceof TInteger i) {
            int index = i.getValue();
            if (index < 0 || index >= content.size()) {
                thread.reportRuntimeError("index " + index + " out of bounds for length " + content.size());
                return null;
            }
            return content.get(index);
        }
        else {
            Range range = (Range) key;
            int from = range.getFrom();
            if (from < 0) from = 0;
            int to = range.getTo();
            if (to > content.size()) to = content.size();
            ArrayList<TObject> subList = new ArrayList<>(content.subList(from, to));
            return new TArray(subList);
        }
    }

    @Override
    public boolean writeToContainer(TThread thread, TObject key, TObject value) {
        if (!(key instanceof TInteger i)){
            thread.reportRuntimeError("invalid key " + key.getType() + ": <Integer> expected");
            return false;
        }
        List<TObject> content = getValue();
        int index = i.getValue();
        if (index < 0 || index >= content.size()) {
            thread.reportRuntimeError("index " + index + " out of bounds for length " + content.size());
            return false;
        }
        content.set(index, value);
        return true;
    }

    @Override
    public IteratorObject iterator() {
        // make a copy to avoid ConcurrentModificationException
        ArrayList<TObject> copy = new ArrayList<>(getValue());
        return new JCFIteratorAdapter(ITR_TYPE, copy.iterator());
    }

    private class PushMethod extends NativeFunction {
        public String getName() {
            return "push";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance().add("item", null);
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            List<TObject> content = TArray.this.getValue();
            content.add(args.get(0));
            return Null.INSTANCE;
        }
    }

    private class PopMethod extends NativeFunction {
        public String getName() {
            return "pop";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }
        public TObject evaluate(Environment env, List<TObject> params) {
            List<TObject> content = TArray.this.getValue();
            return content.remove(content.size()-1);
        }
    }

    private class SizeMethod extends NativeFunction {
        public String getName() {
            return "size";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            List<TObject> content = TArray.this.getValue();
            return new TInteger(content.size());
        }
    }

    private class InsertMethod extends NativeFunction {
        public String getName() {
            return "insert";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance().add("position", null).add("item", null);
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            TObject data = args.get(0);
            if (!(data instanceof TInteger i)) {
                env.reportRuntimeError("<Integer> for index expected");
                return null;
            }
            List<TObject> content = TArray.this.getValue();
            int index = i.getValue();
            if (index < 0 || index > content.size()) {
                env.reportRuntimeError("index " + index + " out of bounds for length " + content.size());
                return null;
            }
            content.add(index, args.get(1));
            return Null.INSTANCE;
        }
    }

    private class RemoveMethod extends NativeFunction {
        public String getName() {
            return "remove";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance().add("range", null);
        }
        public TObject evaluate(Environment env, List<TObject> args) {

            TObject o = args.get(0);

            if (!(o instanceof TInteger || o instanceof Range)){
                env.reportRuntimeError("invalid key " + o.getType() + ": <Integer> or <Range> expected");
                return null;
            }

            List<TObject> content = TArray.this.getValue();
            if (o instanceof TInteger i){
                content.remove((int) i.getValue());
            }
            else {
                Range range = (Range) o;
                Tuple<TInteger, TInteger> r = range.getValue();
                int from = Math.max(0, r.getFirst().getValue());
                int to = Math.min(content.size(), r.getSecond().getValue());
                content.subList(from, to).clear();
            }

            return Null.INSTANCE;
        }
    }

    private class KeysMethod extends NativeFunction {
        public String getName() {
            return "keys";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            List<TObject> content = TArray.this.getValue();
            return new Range(0, content.size());
        }
    }

    private class ValuesMethod extends NativeFunction {
        public String getName() {
            return "values";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            return TArray.this;
        }
    }

    private class SortMethod extends NativeFunction {

        @Override
        public String getName() {
            return "sort";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("comparator", Null.INSTANCE);
        }

        private static class Stop extends RuntimeException {}

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            if (arguments.get(0) == Null.INSTANCE){
                try {
                    getValue().sort((o1, o2) -> {

                        if (o1 instanceof TInteger i1){
                            if (o2 instanceof TInteger i2){
                                return Integer.compare(i1.getValue(), i2.getValue());
                            }
                            if (o2 instanceof TReal i2){
                                return Double.compare(i1.getValue().doubleValue(), i2.getValue());
                            }
                        }
                        else if (o1 instanceof TReal i1){
                            if (o2 instanceof TInteger i2){
                                return Double.compare(i1.getValue(), i2.getValue().doubleValue());
                            }
                            if (o2 instanceof TReal i2){
                                return Double.compare(i1.getValue(), i2.getValue());
                            }
                        }

                        env.reportRuntimeError("order-related sorting requires numeric contents only");
                        throw new Stop();
                    });
                }
                catch (Stop e) {
                    // error occurred in sort
                    return null;
                }
                return Null.INSTANCE;
            }

            if (!(arguments.get(0) instanceof Function comparator)) {
                env.reportRuntimeError("comparator must be type of Function");
                return null;
            }

            try {
                getValue().sort((o1, o2) -> {
                    TObject result = env.call(comparator, List.of(o1, o2));
                    if (result == null) throw new Stop();
                    if (result.getType() != TInteger.TYPE){
                        env.reportRuntimeError("comparator must return an Integer");
                        throw new Stop();
                    }
                    return ((TInteger)result).getValue();
                });
            }
            catch (Stop e) {
                // error occurred in sort
                return null;
            }

            return Null.INSTANCE;
        }

    }


    public static class ConcatStaticMethod extends NativeFunction {

        @Override
        public String getName() {
            return "concat";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("first", null)
                    .add("second", null);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            if (!(arguments.get(0) instanceof TArray first)) {
                env.reportRuntimeError("Array expected; got " + arguments.get(0).getType());
                return null;
            }

            if (!(arguments.get(1) instanceof TArray second)) {
                env.reportRuntimeError("Array expected; got " + arguments.get(1).getType());
                return null;
            }

            List<TObject> concat = new ArrayList<>(first.getValue());
            concat.addAll(second.getValue());
            return new TArray(concat);
        }
    }

}
