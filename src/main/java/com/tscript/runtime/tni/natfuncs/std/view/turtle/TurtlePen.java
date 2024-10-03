package com.tscript.runtime.tni.natfuncs.std.view.turtle;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TBoolean;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class TurtlePen extends NativeFunction {

    @Override
    public String getName() {
        return "pen";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("down", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject obj = arguments.get(0);

        if (obj.getType() != TBoolean.TYPE){
            env.reportRuntimeError("<Boolean> expected but got " + obj.getType().getDisplayName());
            return null;
        }

        TurtleState.getInstance(env).setDown(
                ((TBoolean) obj).getValue()
        );

        return Null.INSTANCE;
    }
}
