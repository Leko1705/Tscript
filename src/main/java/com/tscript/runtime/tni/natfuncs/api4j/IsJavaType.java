package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TBoolean;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.List;

public final class IsJavaType extends NativeFunction {

    @Override
    public String getName() {
        return "isJavaType";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("type", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject obj = arguments.get(0);
        return TBoolean.of(obj instanceof JavaType);
    }
}
