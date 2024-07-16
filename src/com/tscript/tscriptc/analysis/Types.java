package com.tscript.tscriptc.analysis;

import com.tscript.tscriptc.tree.Operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Types {

    private Types(){}

    private static final Map<String, Set<String>> numericOperableTypeMap = new HashMap<>(){{
        put("Integer", Set.of("Integer", "Real"));
        put("Real", Set.of("Integer", "Real"));
    }};

    private static final Map<String, Set<String>> onlyIntegerOperableMap =
            Map.of("Integer", Set.of("Integer"));

    private static final Map<String, Set<String>> logicOperableTypeMap = new HashMap<>(){{
        put("Integer", Set.of("Integer", "Real"));
        put("Real", Set.of("Integer", "Real"));
    }};

    private static final Map<Operation, Map<String, Set<String>>> operableMap = new HashMap<>(){{
       put(Operation.ADD, numericOperableTypeMap);
       put(Operation.SUB, numericOperableTypeMap);
       put(Operation.MUL, numericOperableTypeMap);
       put(Operation.DIV, numericOperableTypeMap);
        put(Operation.POW, numericOperableTypeMap);
        put(Operation.LESS, numericOperableTypeMap);
        put(Operation.GREATER, numericOperableTypeMap);
        put(Operation.LESS_EQ, numericOperableTypeMap);
        put(Operation.GREATER_EQ, numericOperableTypeMap);
        put(Operation.IDIV, onlyIntegerOperableMap);
        put(Operation.MOD, onlyIntegerOperableMap);
        put(Operation.SHIFT_AL, onlyIntegerOperableMap);
        put(Operation.SHIFT_AR, onlyIntegerOperableMap);
        put(Operation.SHIFT_LR, onlyIntegerOperableMap);
        put(Operation.AND, logicOperableTypeMap);
        put(Operation.OR, logicOperableTypeMap);
        put(Operation.XOR, logicOperableTypeMap);
    }};

    public static boolean canSign(String type){
        return type == null || type.equals("Integer") || type.equals("Real");
    }

    public static boolean canOperateNot(String type){
        return type == null || type.equals("Integer") || type.equals("Boolean");
    }

    public static boolean canOperate(String left, String right, Operation operation){
        if (operation == Operation.ADD && (left.equals("String") || right.equals("String")))
            return true;
        if (operation == Operation.EQUALS || operation == Operation.NOT_EQUALS)
            return true;
        Map<String, Set<String>> opMap = operableMap.get(operation);
        if (!opMap.containsKey(left)) return false;
        Set<String> candidates = opMap.get(left);
        if (candidates != null && candidates.contains(right)) return true;
        candidates = opMap.get(right);
        return candidates != null && candidates.contains(right);
    }

    public static String getTypeFromOperation(String left, String right, Operation operation){
        if (left != null && right != null
                && (left.equals("String") || right.equals("String"))
                && operation == Operation.ADD)
            return "String";
        return null;
    }

    public static boolean isContainerAccessible(String type){
        return type == null || Set.of("Array", "Dictionary", "String", "Range").contains(type);
    }


    public static boolean isIterable(String iterableType) {
        return iterableType == null
                || Set.of("Range", "Array", "Dictionary", "String").contains(iterableType);
    }

    public static boolean isBuildInType(String type) {
        return Set.of("Integer", "Real", "NullType", "Boolean",
                "String", "Function", "Type", "Array", "Dictionary",
                "Range").contains(type);
    }

    public static boolean isCallable(String calledType) {
        return calledType == null || Set.of("Function", "Type").contains(calledType);
    }
}
