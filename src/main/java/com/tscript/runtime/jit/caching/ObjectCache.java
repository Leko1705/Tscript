package com.tscript.runtime.jit.caching;

import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.typing.Type;

import java.util.HashMap;
import java.util.Map;

public class ObjectCache {

    private static int nextId = Integer.MIN_VALUE;

    private static final Map<Object, Integer> typeIds = new HashMap<>();
    private static final Map<Integer, Object> types = new HashMap<>();

    @JITSensitive
    public static int putId(Object type) {
        return typeIds.computeIfAbsent(type, t -> {
            int id = nextId++;
            if (id == -1) id = nextId++; // -1 is failure indicator, do not associate
            types.put(id, type);
            return id;
        });
    }

    @JITSensitive
    public static int getId(Object type) {
        return typeIds.getOrDefault(type, -1);
    }

    @JITSensitive
    public static Object getObject(int id) {
        return types.get(id);
    }

    public static void removeId(Type type) {
        int id = typeIds.remove(type);
        types.remove(id);
    }

}
