package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public final class NativeError extends NativeFunction {

    @Override
    public String getName() {
        return "error";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("message", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        env.reportRuntimeError(arguments.get(0));
        env.getCurrentThread().getVM().exit(1);
        return null;
    }
}
