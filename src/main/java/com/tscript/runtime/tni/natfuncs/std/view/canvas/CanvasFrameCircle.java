package com.tscript.runtime.tni.natfuncs.std.view.canvas;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public final class CanvasFrameCircle extends NativeFunction {

    CanvasFillCircle filler = new CanvasFillCircle();
    CanvasCircle outline = new CanvasCircle();

    @Override
    public String getName() {
        return "frameCircle";
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
        if (outline.evaluate(env, arguments) == null)
            return null;
        return filler.evaluate(env, arguments);
    }
}
