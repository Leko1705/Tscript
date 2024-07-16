package com.tscript.runtime.jit;

import com.tscript.runtime.core.Data;

@JITSensitive
public class GeneratedJITException extends RuntimeException {
    public Data thrown;
    public GeneratedJITException(Data thrown){
        this.thrown = thrown;
    }

}
