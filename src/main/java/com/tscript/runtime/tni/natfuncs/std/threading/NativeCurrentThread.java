package com.tscript.runtime.tni.natfuncs.std.threading;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class NativeCurrentThread extends NativeFunction {

    @Override
    public String getName() {
        return "currentThread";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance();
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        return env.getCurrentThread();
    }
}
