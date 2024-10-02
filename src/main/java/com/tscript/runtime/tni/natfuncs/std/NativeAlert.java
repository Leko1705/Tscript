package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;

import javax.swing.*;
import java.util.List;

public class NativeAlert extends NativeFunction {

    @Override
    public String getName() {
        return "alert";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("message", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        JOptionPane.showMessageDialog(null, TNIUtils.toString(env, arguments.get(0)));
        return Null.INSTANCE;
    }
}
