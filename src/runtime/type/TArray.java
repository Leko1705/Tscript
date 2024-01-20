package runtime.type;

import runtime.core.Data;
import runtime.core.TThread;

import java.util.*;

public class TArray extends PrimitiveObject<List<Data>>
        implements ContainerAccessible, ContainerWriteable, IterableObject {

    private static final TType type = new TType("Array", null);

    public TArray(ArrayList<Data> content) {
        super(content);
        initLater(List.of(
                new Member("push", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new PushMethod()),
                new Member("pop", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new PopMethod()),
                new Member("size", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new PopMethod()),
                new Member("insert", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new InsertMethod()),
                new Member("remove", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new RemoveMethod()),
                new Member("keys", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new KeysMethod()),
                new Member("values", Visibility.PUBLIC, Member.Kind.IMMUTABLE, new ValuesMethod())));
    }

    public TArray(){
        this(new ArrayList<>());
    }


    @Override
    public Data readFromContainer(TThread thread, Data key) {
        TObject o = thread.unpack(key);
        if (!(o instanceof TInteger || o instanceof TRange)){
            thread.reportRuntimeError("invalid key " + o.getType() + ": <Integer> or <Range> expected");
            return null;
        }
        List<Data> content = get();
        if (o instanceof TInteger i) {
            int index = i.get();
            if (index < 0 || index >= content.size()) {
                thread.reportRuntimeError("index " + index + " out of bounds for length " + content.size());
                return null;
            }
            return content.get(index);
        }
        else {
            TRange range = (TRange) o;
            int from = range.get().getFirst().get();
            if (from < 0) from = 0;
            int to = range.get().getSecond().get();
            if (to > content.size()) to = content.size();
            ArrayList<Data> subList = new ArrayList<>(content.subList(from, to));
            return new TArray(subList);
        }
    }

    @Override
    public boolean writeToContainer(TThread thread, Data key, Data value) {
        TObject o = thread.unpack(key);
        if (!(o instanceof TInteger i)){
            thread.reportRuntimeError("invalid key " + o.getType() + ": <Integer> expected");
            return false;
        }
        List<Data> content = get();
        int index = i.get();
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
        ArrayList<Data> copy = new ArrayList<>(get());
        return IteratorObject.of(copy.iterator());
    }

    @Override
    public TType getType() {
        return type;
    }


    private class PushMethod extends Callable {
        public String getName() {
            return "push";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>(){{put("item", null);}};
        }
        public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
            List<Data> content = TArray.this.get();
            content.add(params.get("item"));
            return TNull.NULL;
        }
    }

    private class PopMethod extends Callable {
        public String getName() {
            return "pop";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>();
        }
        public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
            List<Data> content = TArray.this.get();
            return content.remove(content.size()-1);
        }
    }

    private class SizeMethod extends Callable {
        public String getName() {
            return "size";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>();
        }
        public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
            List<Data> content = TArray.this.get();
            return new TInteger(content.size());
        }
    }

    private class InsertMethod extends Callable {
        public String getName() {
            return "insert";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>(){{put("position", null);put("item", null);}};
        }
        public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
            Data data = caller.unpack(params.get("position"));
            if (!(data instanceof TInteger i)) {
                caller.reportRuntimeError("<Integer> for index expected");
                return null;
            }
            List<Data> content = TArray.this.get();
            int index = i.get();
            if (index < 0 || index > content.size()) {
                caller.reportRuntimeError("index " + index + " out of bounds for length " + content.size());
                return null;
            }
            content.add(index, params.get("item"));
            return TNull.NULL;
        }
    }

    private class RemoveMethod extends Callable {
        public String getName() {
            return "remove";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>(){{
                put("range", null);
            }};
        }
        public Data eval(TThread caller, LinkedHashMap<String, Data> params) {

            TObject o = caller.unpack(params.get("range"));

            if (!(o instanceof TInteger || o instanceof TRange)){
                caller.reportRuntimeError("invalid key " + o.getType() + ": <Integer> or <Range> expected");
                return null;
            }

            List<Data> content = TArray.this.get();
            if (o instanceof TInteger i){
                content.remove((int) i.get());
            }
            else {
                TRange range = (TRange) o;
                Tuple<TInteger, TInteger> r = range.get();
                int from = Math.max(0, r.getFirst().get());
                int to = Math.min(content.size(), r.getSecond().get());
                content.subList(from, to).clear();
            }

            return TNull.NULL;
        }
    }

    private class KeysMethod extends Callable {
        public String getName() {
            return "keys";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>();
        }
        public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
            List<Data> content = TArray.this.get();
            return new TRange(0, content.size());
        }
    }

    private class ValuesMethod extends Callable {
        public String getName() {
            return "values";
        }
        public LinkedHashMap<String, Data> getParameters() {
            return new LinkedHashMap<>();
        }
        public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
            return TArray.this;
        }
    }

}
