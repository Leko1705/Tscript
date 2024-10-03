package com.tscript.runtime.tni.natfuncs.std.view.canvas;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.natfuncs.std.view.ViewManager;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class CanvasClear extends NativeFunction {

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance();
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        ViewManager.getInstance(env, "canvas").clear();
        return Null.INSTANCE;
    }
}
