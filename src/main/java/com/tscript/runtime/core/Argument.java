package com.tscript.runtime.core;

import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.typing.PrimitiveObject;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;
import com.tscript.runtime.utils.Tuple;

public class Argument extends PrimitiveObject<Tuple<String, TObject>> {

    @JITSensitive
    protected Argument(TObject value){
        this(null, value);
    }

    @JITSensitive
    protected Argument(String name, TObject value) {
        super(new Tuple<>(name, value));
    }

    @Override
    public Type getType() {
        throw new ExecutionException("Argument type not supported");
    }
}
