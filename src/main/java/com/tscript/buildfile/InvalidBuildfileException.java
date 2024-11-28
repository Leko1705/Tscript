package com.tscript.buildfile;

public class InvalidBuildfileException extends RuntimeException {

    public InvalidBuildfileException(Exception cause) {
        super(cause);
    }
}
