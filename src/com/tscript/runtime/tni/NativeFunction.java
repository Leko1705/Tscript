package com.tscript.runtime.tni;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.type.Callable;
import com.tscript.runtime.type.TNull;

import java.util.LinkedHashMap;

public abstract class NativeFunction extends Callable {

    @Override
    public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
        return run(caller, params);
    }

    public abstract Data run(TThread caller, LinkedHashMap<String, Data> params);

}
