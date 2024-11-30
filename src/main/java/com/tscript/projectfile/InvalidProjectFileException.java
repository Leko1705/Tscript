package com.tscript.projectfile;

public class InvalidProjectFileException extends RuntimeException {

    public InvalidProjectFileException(String msg, Exception cause) {
        super(msg, cause);
    }
}
