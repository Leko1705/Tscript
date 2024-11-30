package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TBoolean;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.TString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class NativeExists extends NativeFunction {

    @Override
    public String getName() {
        return "exists";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("key", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject data = arguments.get(0);
        if (!(data instanceof TString s)){
            env.reportRuntimeError("string expected; got " + data);
            return null;
        }

        String path = s.getValue();

        try {
            return TBoolean.of(Files.exists(Path.of(path)));
        }
        catch (SecurityException e){
            env.reportRuntimeError("can not eval exist() due to missing security permissions");
            return null;
        }
    }
}
