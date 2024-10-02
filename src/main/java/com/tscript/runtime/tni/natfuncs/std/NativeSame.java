package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TBoolean;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class NativeSame extends NativeFunction {

    @Override
    public String getName() {
        return "same";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("first", null)
                .add("second", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        return TBoolean.of(TNIUtils.areEqual(arguments.get(0), arguments.get(1)));
    }
}
