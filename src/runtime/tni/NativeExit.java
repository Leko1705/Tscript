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
    public Data run(TThread caller, LinkedHashMap<String, Data> params) {

        TObject exitCode = caller.unpack(params.get("status"));

        if (!(exitCode instanceof TInteger i)){
            caller.reportRuntimeError("<Integer> is required fo exist code");
            return null;
        }

        System.exit(i.get());
        return null;
    }
}
