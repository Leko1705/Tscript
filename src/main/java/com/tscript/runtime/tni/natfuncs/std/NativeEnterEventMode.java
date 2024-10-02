package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class NativeEnterEventMode extends NativeFunction {

    @Override
    public String getName() {
        return "enterEventMode";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance();
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        if (!env.getCurrentThread().isMainThread()){
            env.reportRuntimeError("event manager is only runnable from main thread");
            return null;
        }
        return EventManager.getInstance().enterEventMode(env);
    }
}
