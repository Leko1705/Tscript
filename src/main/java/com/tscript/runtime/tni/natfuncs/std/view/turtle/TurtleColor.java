package com.tscript.runtime.tni.natfuncs.std.view.turtle;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;

import java.awt.*;
import java.util.List;

public final class TurtleColor extends NativeFunction {

    @Override
    public String getName() {
        return "color";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("red", null)
                .add("green", null)
                .add("blue", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        Double r = extractValue(arguments.get(0), env);
        if (r == null) return null;
        Double g = extractValue(arguments.get(1), env);
        if (g == null) return null;
        Double b = extractValue(arguments.get(2), env);
        if (b == null) return null;

        TurtleState.getInstance(env).setColor(new Color(r.floatValue(), g.floatValue(), b.floatValue()));

        return Null.INSTANCE;
    }

    private Double extractValue(TObject obj, Environment env){
        double val;

        if (obj.getType() == TReal.TYPE){
            val = ((TReal)obj).getValue();
        }
        else if (obj.getType() == TInteger.TYPE){
            val = (double) ((TInteger)obj).getValue();
        }
        else {
            env.reportRuntimeError("<Integer> or <Real> expected but got " + obj.getType().getDisplayName());
            return null;
        }

        if (val < 0 || val > 1){
            env.reportRuntimeError("value must be in range of [0, 1]");
            return null;
        }

        return val;
    }
}
