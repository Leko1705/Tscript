package runtime.tni;

import runtime.core.Data;
import runtime.core.TThread;
import runtime.type.Callable;
import runtime.type.TNull;

import java.util.LinkedHashMap;

public abstract class NativeFunction extends Callable {

    @Override
    public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
        return run(caller, params);
    }

    public abstract Data run(TThread caller, LinkedHashMap<String, Data> params);

}
