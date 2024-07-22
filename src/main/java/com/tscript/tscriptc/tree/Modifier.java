package com.tscript.tscriptc.tree;

/**
 * Enums that represent the actual modifiers.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public enum Modifier {

    PUBLIC("public"),

    PROTECTED("protected"),

    PRIVATE("private"),

    STATIC("static"),

    NATIVE("native"),

    OVERRIDDEN("overridden"),

    ABSTRACT("abstract"),

    CONSTANT("const");


    public final String name;

    Modifier(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }

    public boolean isVisibility(){
        return switch (this){
            case PUBLIC,
                 PRIVATE,
                 PROTECTED -> true;
            default -> false;
        };
    }

}
