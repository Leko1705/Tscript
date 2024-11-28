package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TBoolean;
import com.tscript.runtime.typing.TObject;

import javax.swing.*;
import java.util.List;

public final class NativeConfirm extends NativeFunction {

    @Override
    public String getName() {
        return "confirm";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("message", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        int dialogResult = JOptionPane.showConfirmDialog(null, TNIUtils.toString(env, arguments.get(0)));
        if(dialogResult == JOptionPane.YES_OPTION){
            return TBoolean.TRUE;
        }
        else {
            return TBoolean.FALSE;
        }
    }
}
