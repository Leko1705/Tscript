package com.tscript.runtime.jit.caching;

import com.tscript.runtime.jit.JITSensitive;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Associates objects with integer ids, so that compiled code can refer to runtime objects
 * (types, constants, deoptimization information) by embedding an int constant.
 * Objects are identified by identity.
 */
public class ObjectCache {

    private static int nextId = 0;

    private static final Map<Object, Integer> ids = new IdentityHashMap<>();
    private static volatile Object[] objects = new Object[256];

    @JITSensitive
    public static synchronized int putId(Object object) {
        Integer id = ids.get(object);
        if (id != null) return id;
        id = nextId++;
        ids.put(object, id);
        Object[] array = objects;
        if (id >= array.length) {
            array = Arrays.copyOf(array, array.length * 2);
        }
        array[id] = object;
        objects = array;
        return id;
    }

    /**
     * @return the id of the object or -1 if it has none
     */
    @JITSensitive
    public static synchronized int getId(Object object) {
        return ids.getOrDefault(object, -1);
    }

    @JITSensitive
    public static Object getObject(int id) {
        Object[] array = objects;
        return id >= 0 && id < array.length ? array[id] : null;
    }

    public static synchronized void removeId(Object object) {
        Integer id = ids.remove(object);
        if (id != null) objects[id] = null;
    }

}
