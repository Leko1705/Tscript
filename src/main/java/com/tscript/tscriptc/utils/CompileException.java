package com.tscript.tscriptc.utils;

public class CompileException extends RuntimeException {

    public final String message;
    public final Location location;
    public final Phase phase;

    public CompileException(String msg, Location location, Phase phase){
        super(format(msg, location));
        this.message = msg;
        this.location = location;
        this.phase = phase;
    }

    private static String format(String msg, Location location){
        return msg + " (in line " + location.line() + ")";
    }

}
