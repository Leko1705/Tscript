package com.tscript.tscriptc.tree;

/**
 * Enums that represent the actual operation type.
 *
 * @see BinaryOperationTree
 * @since 1.0
 * @author Lennart Köhler
 */
public enum Operation {

    ADD("+"),

    SUB("-"),

    MUL("*"),

    DIV("/"),

    IDIV("//"),

    MOD("%"),

    POW("^"),

    AND("and"),

    OR("or"),

    XOR("xor"),

    SHIFT_AL("<<"),

    SHIFT_AR(">>"),

    SHIFT_LR(">>>"),

    LESS("<"),

    GREATER(">"),

    LESS_EQ("<="),

    GREATER_EQ(">="),

    EQUALS("=="),

    NOT_EQUALS("!=");

    Operation(String encoding){
        this.encoding = encoding;
    }

    public final String encoding;

    public static Operation of(String name){
        for (Operation o : values())
            if (name.equals(o.encoding))
                return o;
       throw new AssertionError();
    }

    public static Operation withoutAssign(String name){
        if (!name.endsWith("=")) return of(name);
        return of(name.substring(0, name.length()-1));
    }

}
