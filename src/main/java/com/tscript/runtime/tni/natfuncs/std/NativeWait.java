package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.TObject;

import java.util.List;

public final class NativeWait extends NativeFunction {

    @Override
    public String getName() {
        return "wait";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("ms", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject data = arguments.get(0);
        try {
            Thread.sleep(((TInteger)data).getValue());
        }
        catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        catch (ClassCastException e){
            env.reportRuntimeError("<Integer> expected; got: " + data);
            return null;
        }
        return Null.INSTANCE;
    }
}
