package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;

import java.util.*;

public class LoadJavaClass extends NativeFunction {

    @Override
    public String getName() {
        return "loadClass";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("name", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject arg = arguments.get(0);

        if (arg.getType() != TString.TYPE){
            env.reportRuntimeError("can not resolve java class from input argument of type " + arg.getType());
            return null;
        }

        String name = ((TString) arg).getValue();

        Class<?> clazz;
        try {
            clazz = Class.forName(name);
        }
        catch (Exception e){
            env.reportRuntimeError("can not load java class " + name);
            return null;
        }

        try {
            return JavaAPIStateManager.getInstance(env.getCurrentThread().getVM()).getType(clazz);
        }
        catch (Exception e){
            env.reportRuntimeError("error while inflating java type " + e.getMessage());
            return null;
        }
    }


}
