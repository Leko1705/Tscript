package com.tscript.runtime.tni.natfuncs.std.view.canvas;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class CanvasFrameRect extends NativeFunction {

    private final CanvasFillRect filler = new CanvasFillRect();
    private final CanvasRect outline = new CanvasRect();

    @Override
    public String getName() {
        return "frameRect";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("x", null)
                .add("y", null)
                .add("width", null)
                .add("height", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        if (outline.evaluate(env, arguments) == null)
            return null;
        return filler.evaluate(env, arguments);
    }
}
