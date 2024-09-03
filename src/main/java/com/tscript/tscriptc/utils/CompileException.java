package com.tscript.tscriptc.utils;

public class CompileException extends RuntimeException {

    public final String message;
    public final Location location;
    public final Phase phase;

    public CompileException(String msg, Location location, Phase phase){
        super(msg);
        this.message = msg;
        this.location = location;
        this.phase = phase;
    }

}
