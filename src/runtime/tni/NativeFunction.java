package runtime.tni;

import runtime.core.Data;
import runtime.core.TThread;
import runtime.type.Callable;
import runtime.type.TNull;

import java.util.LinkedHashMap;

public abstract class NativeFunction extends Callable {

    public abstract String getName();

    public abstract LinkedHashMap<String, Data> getParameters();

    public abstract Data eval(TThread caller, Data[] params);

}
