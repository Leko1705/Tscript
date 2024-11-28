package com.tscript.runtime.tni.natfuncs.std.view.canvas;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.tni.natfuncs.std.view.ViewManager;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public final class CanvasText extends NativeFunction {

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("x", null)
                .add("y", null)
                .add("str", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {

        if (!CanvasUtils.requireInteger(arguments.get(0), env))
            return null;


        if (!CanvasUtils.requireInteger(arguments.get(1), env))
            return null;

        String str = TNIUtils.toString(env, arguments.get(2));
        if (str == null) return null;

        ViewManager.getInstance(env, "canvas")
                .drawString(
                        str,
                        ((TInteger) arguments.get(0)).getValue(),
                        ((TInteger) arguments.get(1)).getValue());

        return Null.INSTANCE;
    }
}
