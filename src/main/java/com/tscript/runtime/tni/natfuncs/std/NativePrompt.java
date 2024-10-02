package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.TString;

import javax.swing.*;
import java.util.List;

public class NativePrompt extends NativeFunction {

    private static final TString DEFAULT = new TString("");

    @Override
    public String getName() {
        return "prompt";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("message", null)
                .add("default", DEFAULT);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        String response = JOptionPane.showInputDialog(
                null,
                TNIUtils.toString(env, arguments.get(0)),
                TNIUtils.toString(env, arguments.get(1)));
        if (response == null) return Null.INSTANCE;
        return new TString(response);
    }
}
