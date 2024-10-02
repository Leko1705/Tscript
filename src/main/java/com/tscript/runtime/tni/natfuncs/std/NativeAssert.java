package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class NativeAssert extends NativeFunction {
    @Override
    public String getName() {
        return "assert";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("condition", null)
                .add("message", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject data = arguments.get(0);
        if (!TNIUtils.isTrue(data)){
            env.reportRuntimeError(arguments.get(1));
            return null;
        }
        return Null.INSTANCE;
    }
}
