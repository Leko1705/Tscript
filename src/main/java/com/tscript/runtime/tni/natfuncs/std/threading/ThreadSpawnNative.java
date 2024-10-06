package com.tscript.runtime.tni.natfuncs.std.threading;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TBoolean;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public class ThreadSpawnNative extends NativeFunction {

    @Override
    public String getName() {
        return "Thread_spawn_native";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("func", null)
                .add("daemon", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TThread thread = env.getCurrentThread()
                .getVM()
                .spawnThread((Function)arguments.get(0), List.of());
        if (arguments.get(1) == TBoolean.TRUE)
            thread.setDaemon(true);
        return thread;
    }
}
