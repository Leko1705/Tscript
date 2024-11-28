package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.TString;

import java.util.List;

public final class NativePrint extends NativeFunction {

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("text", new TString(""));
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        String printString = TNIUtils.toString(env, arguments.get(0));
        if (printString == null) return null; // error occurred
        env.getCurrentThread().getVM().getOut().println(printString);
        return Null.INSTANCE;
    }
}
