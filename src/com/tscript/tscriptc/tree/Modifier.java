package com.tscript.tscriptc.tree;

public enum Modifier {

    PUBLIC("public"),
    PROTECTED("protected"),
    PRIVATE("private"),
    STATIC("static"),
    ABSTRACT("abstract"),
    IMMUTABLE("const"),
    OVERRIDDEN("overridden");

    Modifier(String name){
        this.name = name;
    }

    public final String name;

    public static Modifier of(String name){
        for (Modifier m : values())
            if (name.equals(m.name))
                return m;
        return null;
    }

    public boolean isVisibility(){
        return switch (this){
            case PUBLIC, PRIVATE, PROTECTED -> true;
            default -> false;
        };
    }

}
