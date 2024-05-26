package runtime.type;

import runtime.core.Data;
import runtime.core.TThread;

import java.util.*;

public class TDictionary extends PrimitiveObject<Map<Data, Data>>
        implements ContainerAccessible, ContainerWriteable, IterableObject {

    private static final TType type = new TType("Dictionary", null);

    public TDictionary(LinkedHashMap<Data, Data> content){
        super(content);
        initLater(List.of(
                new Member("size", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new SizeMethod()),
                new Member("remove", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new RemoveMethod()),
                new Member("has", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new HasMethod()),
                new Member("keys", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new KeysMethod()),
                new Member("values", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new ValuesMethod())));
    }

    public TDictionary() {
        this(new LinkedHashMap<>());
    }

    @Override
    public Data readFromContainer(TThread thread, Data key) {
        Map<Data, Data> content = get();
        Data value = content.get(key);
        return value != null ? value : TNull.NULL;
    }

    @Override
    public boolean writeToContainer(TThread thread, Data key, Data value) {
        Map<Data, Data> content = get();
        content.put(key, value);
        return true;
    }

    @Override
    public IteratorObject iterator() {
        // make a copy to avoid ConcurrentModificationException
        ArrayList<Data> copy = new ArrayList<>(get().keySet());
        return IteratorObject.of(copy.iterator());
    }

    @Override
    public String toString() {
        Map<Data, Data> content = get();
        if (content.isEmpty()) return "{}";
        Iterator<Data> keyItr = content.keySet().iterator();
        Iterator<Data> valItr = content.values().iterator();

        StringBuilder sb = new StringBuilder("{");
        sb.append(keyItr.next()).append(": ").append(valItr.next());

        while (keyItr.hasNext()){
            sb.append(", ").append(keyItr.next()).append(": ").append(valItr.next());
        }

        return sb.append("}").toString();
    }

    @Override
    public TType getType() {
        return type;
    }


    private class SizeMethod extends Callable {
        public String getName() {
            return "size";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>();
        }
        public Data eval(TThread caller, Data[] params) {
            Map<Data, Data> content = TDictionary.this.get();
            return new TInteger(content.size());
        }
    }

    private class RemoveMethod extends Callable {
        public String getName() {
            return "remove";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>(){{put("key", null);}};
        }
        public Data eval(TThread caller, Data[] params) {
            Map<Data, Data> content = TDictionary.this.get();
            Data removed = content.remove(params[0]);
            return removed != null ? removed : TNull.NULL;
        }
    }

    private class HasMethod extends Callable {
        public String getName() {
            return "has";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>(){{put("key", null);}};
        }
        public Data eval(TThread caller, Data[] params) {
            Map<Data, Data> content = TDictionary.this.get();
            return TBoolean.of(content.containsKey(params[0]));
        }
    }

    private class KeysMethod extends Callable {
        public String getName() {
            return "keys";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>();
        }
        public Data eval(TThread caller, Data[] params) {
            Map<Data, Data> content = TDictionary.this.get();
            return new TArray(new ArrayList<>(content.keySet()));
        }
    }

    private class ValuesMethod extends Callable {
        public String getName() {
            return "values";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>();
        }
        public Data eval(TThread caller, Data[] params) {
            Map<Data, Data> content = TDictionary.this.get();
            return new TArray(new ArrayList<>(content.values()));
        }
    }

}
