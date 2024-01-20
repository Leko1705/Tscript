package runtime.tni;

import runtime.core.Data;
import runtime.core.Reference;
import runtime.core.TThread;
import runtime.type.Callable;
import runtime.type.TNull;
import runtime.type.TObject;
import runtime.type.TString;

import java.util.LinkedHashMap;

public class NativePrint extends NativeFunction {

    private static final TString empty = new TString("");

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public LinkedHashMap<String, Data> getParameters() {
        return new LinkedHashMap<>(){{put("x", empty);}};
    }

    @Override
    public Data run(TThread caller, LinkedHashMap<String, Data> params) {
        System.out.println(makePrintable(caller, params.get("x")));
        return null;
    }

    public static String makePrintable(TThread tThread, Data data) {
        TObject obj = tThread.unpack(data);
        String s = obj.toString();
        if (data.isReference()){
            Reference ref = data.asReference();
            s += "@" + ref.hashCode();
        }
        return s;
    }
}
