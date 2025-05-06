package com.tscript.runtime.jit.caching;

import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.HashMap;
import java.util.Map;

public class TypeCache {

    private static int nextId = Integer.MIN_VALUE;

    private static final Map<Type, Integer> typeIds = new HashMap<>();
    private static final Map<Integer, Type> types = new HashMap<>();

    @JITSensitive
    public static int getId(Type type) {
        return typeIds.computeIfAbsent(type, t -> {
            int id = nextId++;
            types.put(id, type);
            return id;
        });
    }

    @JITSensitive
    public static TObject getType(int id) {
        return types.get(id);
    }

    public static void removeId(Type type) {
        int id = typeIds.remove(type);
        types.remove(id);
    }

}
