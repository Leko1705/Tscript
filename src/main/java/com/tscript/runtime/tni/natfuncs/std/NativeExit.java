package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class NativeExit extends NativeFunction {

    private static final TInteger DEFAULT_STATUS = new TInteger(0);

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("status", DEFAULT_STATUS);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject exitCode = arguments.get(0);

        if (!(exitCode instanceof TInteger i)){
            env.reportRuntimeError("<Integer> is required fo exist code");
            return null;
        }

        env.getCurrentThread().getVM().exit(i.getValue());
        return Null.INSTANCE;
    }
}
