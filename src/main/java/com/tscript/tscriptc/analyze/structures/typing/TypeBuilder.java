package com.tscript.tscriptc.analyze.structures.typing;

import com.tscript.tscriptc.tree.Operation;

import java.util.HashMap;
import java.util.Map;

public class TypeBuilder {

    public static TypeBuilder newBuilder(String name) {
        return new TypeBuilder(name, name);
    }

    public static TypeBuilder newBuilder(String name, String displayName) {
        return new TypeBuilder(name, displayName);
    }

    private final String name;
    private final String displayName;
    private boolean callable = false;
    private boolean itemAccessible = false;
    private final Map<String, Map<Operation, String>> operations = new HashMap<>();

    private TypeBuilder(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        addOperation(Operation.ADD, "String", "String");
    }

    public TypeBuilder setCallable(boolean callable) {
        this.callable = callable;
        return this;
    }

    public TypeBuilder setItemAccessible(boolean itemAccessible) {
        this.itemAccessible = itemAccessible;
        return this;
    }

    public TypeBuilder addOperation(Operation operation, String typeName, String returnType) {
        Map<Operation, String> lowerOpMap = operations.computeIfAbsent(typeName, k -> new HashMap<>());
        lowerOpMap.put(operation, returnType);
        return this;
    }

    public Type create() {
        return new Type() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            protected Type doOperate(Operation operation, Type type, Map<String, Type> typeMap) {
                if (this == typeMap.get("String")) return this;
                Map<Operation, String> lowerOpMap = operations.get(type.getName());
                if (lowerOpMap == null)
                    return null;
                String returnType = lowerOpMap.get(operation);
                if (returnType == null)
                    return UnknownType.INSTANCE;
                return typeMap.get(returnType);
            }

            @Override
            public boolean isCallable() {
                return callable;
            }

            @Override
            public boolean isItemAccessible() {
                return itemAccessible;
            }

            @Override
            public String toString() {
                return displayName;
            }
        };
    }

}
