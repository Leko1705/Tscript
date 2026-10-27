package com.tscript.runtime.jit.tracing;

/**
 * Thrown by compiled code if a runtime error was reported while running it (e.g. inside a
 * called function). The error handling of the interpreter already unwound the frame stack,
 * so compiled code must not continue.
 */
public class ImageAbortException extends RuntimeException {

    public static final ImageAbortException INSTANCE = new ImageAbortException();

    private ImageAbortException() {
        super(null, null, false, false);
    }
}
