package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;

import java.util.List;

public class DropJavaClass extends NativeFunction {

    @Override
    public String getName() {
        return "dropClass";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("dropped", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {

        TObject arg = arguments.get(0);

        JavaAPIStateManager manager = JavaAPIStateManager.getInstance(env.getCurrentThread().getVM());

        if (arg.getType() == TString.TYPE){
            String name = ((TString) arg).getValue();
            try {
                Class<?> clazz = Class.forName(name);
                manager.drop(clazz);
            }
            catch (Exception e){
                env.reportRuntimeError("error while dropping java class: " + e.getMessage());
                return null;
            }
        }
        else if (arg.getType() == TString.TYPE){
            Type t = (Type) arg;

            if (arg instanceof JavaType type){
                manager.drop(type.clazz);
            }
            else {
                env.reportRuntimeError("type " + t.getDisplayName() + " is not a java type");
            }
        }
        else {
            env.reportRuntimeError(
                    "type mismatch -> expected Type<String> or Type<Type> but got " + arg.getType().getDisplayName());
            return null;
        }

        return Null.INSTANCE;
    }
}
