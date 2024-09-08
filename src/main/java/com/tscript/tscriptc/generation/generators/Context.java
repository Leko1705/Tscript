package com.tscript.tscriptc.generation.generators;

import com.tscript.tscriptc.generation.generators.impls.CompFile;

public class Context {

    private final CompFile file;
    private int nextFunctionIndex = 0;
    private int nextClassIndex = 0;
    private int nextLambdaIndex = 0;

    public Context(CompFile file) {
        this.file = file;
    }

    public int getNextFunctionIndex() {
        return nextFunctionIndex++;
    }

    public int getNextClassIndex() {
        return nextClassIndex++;
    }

    public int getNextLambdaIndex() {
        return nextLambdaIndex++;
    }

    public CompFile getFile() {
        return file;
    }
}
