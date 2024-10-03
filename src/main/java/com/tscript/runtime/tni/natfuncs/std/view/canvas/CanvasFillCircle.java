package com.tscript.runtime.tni.natfuncs.std.view.canvas;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.natfuncs.std.view.ViewManager;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class CanvasFillCircle extends NativeFunction {

    @Override
    public String getName() {
        return "fillCircle";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("x", null)
                .add("y", null)
                .add("radius", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {

        if (!CanvasUtils.checkCircleIntArgs(arguments, env))
            return null;

        int x = ((TInteger)arguments.get(0)).getValue();
        int y = ((TInteger)arguments.get(1)).getValue();
        int radius = ((TInteger)arguments.get(2)).getValue();

        ViewManager.getInstance(env, "canvas")
                .fillOval(
                        x - radius / 2,
                        y - radius / 2,
                        radius,
                        radius
                );

        return Null.INSTANCE;
    }
}
