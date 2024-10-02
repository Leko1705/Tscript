package com.tscript.runtime.typing;


import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.utils.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TArray extends PrimitiveObject<List<TObject>>
        implements ContainerWriteableObject, ContainerAccessibleObject, IterableObject {

    public static final Type TYPE =
            new Type.Builder("Array").setConstructor((thread, params) -> Null.INSTANCE).build();

    public static final Type ITR_TYPE =
            new Type.Builder("ArrayIterator")
                    .setAbstract(true)
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
                "values", new Member(Visibility.PUBLIC, false, "values", new ValuesMethod())
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

}
