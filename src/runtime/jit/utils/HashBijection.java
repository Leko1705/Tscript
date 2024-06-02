package runtime.jit.utils;

import java.util.HashMap;
import java.util.Map;

public class HashBijection<K, V> implements Bijection<K, V> {

    private final Map<K, V> to = new HashMap<>();
    private final Map<V, K> back = new HashMap<>();

    @Override
    public void add(K key, V value) {
        to.put(key, value);
        back.put(value, key);
    }

    @Override
    public V getValue(K key) {
        return to.get(key);
    }

    @Override
    public K getKey(V value) {
        return back.get(value);
    }
}
