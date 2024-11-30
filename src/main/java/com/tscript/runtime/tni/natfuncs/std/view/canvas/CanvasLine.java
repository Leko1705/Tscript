package com.tscript.runtime.tni.natfuncs.std.view.canvas;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.natfuncs.std.view.ViewManager;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public final class CanvasLine extends NativeFunction {

    @Override
    public String getName() {
        return "line";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("x1", null)
                .add("y1", null)
                .add("x2", null)
                .add("y2", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {

        if (!CanvasUtils.checkRectangleIntArgs(arguments, env))
            return null;

        ViewManager.getInstance(env, "canvas")
                .drawLine(
                        ((TInteger)arguments.get(0)).getValue(),
                        ((TInteger)arguments.get(1)).getValue(),
                        ((TInteger)arguments.get(2)).getValue(),
                        ((TInteger)arguments.get(3)).getValue()
                );

        return Null.INSTANCE;
    }
}
