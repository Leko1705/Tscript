package com.tscript.compiler.impl.generation.gen;

public class Context {

    private final CompFile file;
    private int nextFunctionIndex = 0;
    private int nextLambdaIndex = 0;

    public Context(CompFile file) {
        this.file = file;
    }

    public int getNextFunctionIndex() {
        return nextFunctionIndex++;
    }

    public int getNextLambdaIndex() {
        return nextLambdaIndex++;
    }

    public CompFile getFile() {
        return file;
    }
}
