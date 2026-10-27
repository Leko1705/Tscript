package com.tscript.runtime.core;

public class ExecutionException extends RuntimeException {

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(Throwable cause, VirtualFrame frame) {
        super("message", cause);
    }

}
