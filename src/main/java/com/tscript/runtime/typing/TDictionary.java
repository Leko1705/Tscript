package com.tscript.runtime.typing;



import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;

import java.util.*;

public class TDictionary extends PrimitiveObject<Map<TObject, TObject>>
        implements ContainerAccessibleObject, ContainerWriteableObject, IterableObject{

    public static final Type TYPE =
            new Type
                    .Builder("Dictionary")
                    .setParameters(Parameters.newInstance()
                            .add("other", Null.INSTANCE))
                    .setConstructor((thread, params) -> {
                        TObject value = params.get(0);

                        if (value == Null.INSTANCE)
                            return new TDictionary();

                        if (value instanceof TDictionary dict){
                            return new TDictionary(dict.getValue());
                        }

                        thread.reportRuntimeError("can not convert " + value.getType() + " to Dictionary");
                        return null;
                    })
                    .build();

    public static final Type ITR_TYPE =
            new Type.Builder("DictionaryIterator")
                    .setAbstract(true)
                    .setConstructor((thread, params) -> Null.INSTANCE)
                    .build();

    private final Map<String, Member> methods;

    public TDictionary(Map<TObject, TObject> value) {
        super(value);
        methods = new HashMap<>(Map.of(
                "size", Member.of(Visibility.PUBLIC, false, "size", new SizeMethod()),
                "has", Member.of(Visibility.PUBLIC, false, "has", new HasMethod()),
                "remove", Member.of(Visibility.PUBLIC, false, "remove", new RemoveMethod()),
                "keys", Member.of(Visibility.PUBLIC, false, "keys", new KeysMethod()),
                "values", Member.of(Visibility.PUBLIC, false, "values", new ValuesMethod())
        ));
    }

    public TDictionary() {
        this(new LinkedHashMap<>());
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
        Map<TObject, TObject> content = getValue();
        TObject value = content.get(key);
        return value != null ? value : Null.INSTANCE;
    }

    @Override
    public boolean writeToContainer(TThread thread, TObject key, TObject value) {
        Map<TObject, TObject> content = getValue();
        content.put(key, value);
        return true;
    }

    @Override
    public IteratorObject iterator() {
        // make a copy to avoid ConcurrentModificationException
        ArrayList<TObject> copy = new ArrayList<>(getValue().keySet());
        return new JCFIteratorAdapter(ITR_TYPE, copy.iterator());
    }

    @Override
    public String getDisplayName() {
        StringBuilder builder = new StringBuilder("{");
        Iterator<Map.Entry<TObject, TObject>> itr = getValue().entrySet().iterator();
        if (itr.hasNext()) {
            Map.Entry<TObject, TObject> entry = itr.next();
            builder.append(entry.getKey()).append(": ").append(entry.getValue());
            while (itr.hasNext()) {
                entry = itr.next();
                builder.append(", ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue());
            }
        }
        return builder.append("}").toString();
    }

    private class SizeMethod extends NativeFunction {
        public String getName() {
            return "size";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            Map<TObject, TObject> content = TDictionary.this.getValue();
            return new TInteger(content.size());
        }
    }

    private class RemoveMethod extends NativeFunction {
        public String getName() {
            return "remove";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance().add("key", null);
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            Map<TObject, TObject> content = TDictionary.this.getValue();
            TObject removed = content.remove(args.get(0));
            return removed != null ? removed : Null.INSTANCE;
        }
    }

    private class HasMethod extends NativeFunction {
        public String getName() {
            return "has";
        }
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance().add("key", null);
        }
        public TObject evaluate(Environment env, List<TObject> args) {
            Map<TObject, TObject> content = TDictionary.this.getValue();
            return TBoolean.of(content.containsKey(args.get(0)));
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
            Map<TObject, TObject> content = TDictionary.this.getValue();
            return new TArray(new ArrayList<>(content.keySet()));
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
            Map<TObject, TObject> content = TDictionary.this.getValue();
            return new TArray(new ArrayList<>(content.values()));
        }
    }
}
