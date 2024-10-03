package com.tscript.runtime.tni.natfuncs.std.view.turtle;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.natfuncs.std.view.canvas.CanvasUtils;
import com.tscript.runtime.typing.*;

import java.util.List;

public class TurtleReset extends NativeFunction {

    private static final TInteger ZERO = new TInteger(0);

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("x", ZERO)
                .add("y", ZERO)
                .add("degrees", ZERO)
                .add("down", TBoolean.TRUE);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {

        if (!CanvasUtils.requireInteger(arguments.get(0), env))
            return null;

        if (!CanvasUtils.requireInteger(arguments.get(1), env))
            return null;

        if (!CanvasUtils.requireInteger(arguments.get(2), env))
            return null;

        TObject bool = arguments.get(3);
        if (bool.getType() != TBoolean.TYPE){
            env.reportRuntimeError("<Boolean> expected but got " + bool.getType().getDisplayName());
            return null;
        }

        TurtleState.getInstance(env)
                .reset(
                        ((TInteger)arguments.get(0)).getValue(),
                        ((TInteger)arguments.get(1)).getValue(),
                        ((TInteger)arguments.get(2)).getValue(),
                        ((TBoolean)arguments.get(3)).getValue()
                );

        return Null.INSTANCE;
    }

}
