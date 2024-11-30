package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TArray;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public final class NativeListKeys extends NativeFunction {

    @Override
    public String getName() {
        return "listKeys";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance();
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        return new TArray();
    }
}
