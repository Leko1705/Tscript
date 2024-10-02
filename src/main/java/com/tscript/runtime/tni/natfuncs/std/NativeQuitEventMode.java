package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class NativeQuitEventMode extends NativeFunction {

    @Override
    public String getName() {
        return "quitEventMode";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("result", Null.INSTANCE);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        EventManager.getInstance().quitEventMode(arguments.get(0));
        return Null.INSTANCE;
    }
}
