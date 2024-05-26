package runtime.tni;

import runtime.core.Data;
import runtime.core.TThread;
import runtime.type.*;

import java.util.LinkedHashMap;

public class NativeExit extends NativeFunction {

    private static final TInteger default_ = new TInteger(0);


    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public LinkedHashMap<String, Data> getParameters() {
        return new LinkedHashMap<>(){{put("status", default_);}};
    }

    @Override
    public Data eval(TThread caller, Data[] params) {

        TObject exitCode = caller.unpack(params[0]);

        if (!(exitCode instanceof TInteger i)){
            caller.reportRuntimeError("<Integer> is required fo exist code");
            return null;
        }

        System.exit(i.get());
        return TNull.NULL;
    }
}
