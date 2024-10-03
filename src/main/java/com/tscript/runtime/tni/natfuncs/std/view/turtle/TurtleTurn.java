package com.tscript.runtime.tni.natfuncs.std.view.turtle;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.natfuncs.std.view.canvas.CanvasUtils;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class TurtleTurn extends NativeFunction {

    @Override
    public String getName() {
        return "turn";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("degrees", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        if (!CanvasUtils.requireInteger(arguments.get(0), env))
            return null;

        TurtleState.getInstance(env).turn(
                ((TInteger)arguments.get(0)).getValue()
        );

        return Null.INSTANCE;
    }
}
