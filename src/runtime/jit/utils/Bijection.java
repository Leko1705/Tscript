package runtime.jit.utils;

public interface Bijection<K, V> {

    void add(K key, V value);

    V getValue(K key);

    K getKey(V value);

}
