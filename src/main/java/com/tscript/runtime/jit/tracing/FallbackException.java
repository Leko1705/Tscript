package com.tscript.runtime.jit.tracing;


public class FallbackException extends RuntimeException {

    public final int guardId;

    public FallbackException(int guardId) {
        this.guardId = guardId;
    }

}
