package com.tscript.compiler.impl.analyze.structures.typing;

import com.tscript.compiler.source.tree.Operation;

import java.util.HashMap;
import java.util.Map;

public class BuiltInTypes {

    public static Map<String, Type> get() {
        Map<String, Type> map = new HashMap<>();

        registerType(map, TypeBuilder.newBuilder("Function")
                .setCallable(true)
                .create());

        registerType(map, TypeBuilder.newBuilder("Type")
                .setCallable(true)
                .create());

        registerType(map, TypeBuilder.newBuilder("String")
                .setItemAccessible(true)
                .create());

        registerType(map, TypeBuilder.newBuilder("Null", "null").create());

        registerType(map, TypeBuilder.newBuilder("Integer")
                .addOperation(Operation.ADD, "Integer", "Integer")
                .addOperation(Operation.ADD, "Real", "Real")
                .addOperation(Operation.SUB, "Integer", "Integer")
                .addOperation(Operation.SUB, "Real", "Real")
                .addOperation(Operation.MUL, "Integer", "Integer")
                .addOperation(Operation.MUL, "Real", "Real")
                .addOperation(Operation.DIV, "Integer", "Real")
                .addOperation(Operation.DIV, "Real", "Real")
                .addOperation(Operation.IDIV, "Integer", "Integer")
                .addOperation(Operation.MOD, "Integer", "Integer")
                .addOperation(Operation.POW, "Integer", "Real")
                .addOperation(Operation.POW, "Real", "Real")
                .addOperation(Operation.SHIFT_AL, "Integer", "Integer")
                .addOperation(Operation.SHIFT_AR, "Integer", "Integer")
                //.addOperation(Operation.SLR, "Integer", "Integer")
                .addOperation(Operation.AND, "Integer", "Integer")
                .addOperation(Operation.OR, "Integer", "Integer")
                .addOperation(Operation.XOR, "Integer", "Integer")
                .addOperation(Operation.GREATER, "Integer", "Boolean")
                .addOperation(Operation.GREATER, "Real", "Boolean")
                .addOperation(Operation.GREATER_EQ, "Integer", "Boolean")
                .addOperation(Operation.GREATER_EQ, "Real", "Boolean")
                .addOperation(Operation.LESS, "Integer", "Boolean")
                .addOperation(Operation.LESS, "Real", "Boolean")
                .addOperation(Operation.LESS_EQ, "Integer", "Boolean")
                .addOperation(Operation.LESS_EQ, "Real", "Boolean")
                .create());

        registerType(map, TypeBuilder.newBuilder("Real")
                .addOperation(Operation.ADD, "Integer", "Real")
                .addOperation(Operation.ADD, "Real", "Real")
                .addOperation(Operation.SUB, "Integer", "Real")
                .addOperation(Operation.SUB, "Real", "Real")
                .addOperation(Operation.MUL, "Integer", "Real")
                .addOperation(Operation.MUL, "Real", "Real")
                .addOperation(Operation.DIV, "Integer", "Real")
                .addOperation(Operation.DIV, "Real", "Real")
                .addOperation(Operation.POW, "Integer", "Real")
                .addOperation(Operation.POW, "Real", "Real")
                .addOperation(Operation.GREATER, "Integer", "Boolean")
                .addOperation(Operation.GREATER, "Real", "Boolean")
                .addOperation(Operation.GREATER_EQ, "Integer", "Boolean")
                .addOperation(Operation.GREATER_EQ, "Real", "Boolean")
                .addOperation(Operation.LESS, "Integer", "Boolean")
                .addOperation(Operation.LESS, "Real", "Boolean")
                .addOperation(Operation.LESS_EQ, "Integer", "Boolean")
                .create());

        registerType(map, TypeBuilder.newBuilder("Boolean")
                .addOperation(Operation.AND, "Boolean", "Boolean")
                .addOperation(Operation.OR, "Boolean", "Boolean")
                .addOperation(Operation.XOR, "Boolean", "Boolean")
                .create());

        registerType(map, TypeBuilder.newBuilder("Range")
                .setItemAccessible(true)
                .create());

        registerType(map, TypeBuilder.newBuilder("Array")
                .setItemAccessible(true)
                .create());

        registerType(map, TypeBuilder.newBuilder("Dictionary")
                .setItemAccessible(true)
                .create());

        return map;
    }


    private static void registerType(Map<String, Type> typeMap, Type type) {
        typeMap.put(type.getName(), type);
    }


}
