package com.tscript.runtime.tni;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.core.Reference;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.type.Callable;
import com.tscript.runtime.type.TNull;
import com.tscript.runtime.type.TObject;
import com.tscript.runtime.type.TString;

import java.util.LinkedHashMap;
import java.util.List;

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
        try {
            String print = makePrintable(caller, params.get("x"));
            if (print == null) return null;
            System.out.println(print);
            return TNull.NULL;
        }catch (Exception e){
            return null;
        }
    }

    public static String makePrintable(TThread tThread, Data data) {
        TObject obj = tThread.unpack(data);
        int toStringAddress = obj.getIndex("__str__");
        if (toStringAddress == -1)
            return getDefaultPrintable(obj, data);
        else
            return getDefinedPrintable(tThread, obj, data, toStringAddress);
    }

    private static String getDefinedPrintable(TThread thread,
                                              TObject obj,
                                              Data data,
                                              int address){
        TObject candidate = thread.unpack(obj.get(address).data);
        if (candidate instanceof Callable c && c.getParameters().isEmpty()){
            Data toPrint = thread.call(c, List.of());
            if (toPrint == null) return null;
            if (toPrint == c.getOwner()) {
                thread.reportRuntimeError("internalStackOverflowError in print: unable to print owner recursively");
                return null;
            }
            return makePrintable(thread, toPrint);
        }
        else {
            return getDefaultPrintable(obj, data);
        }
    }

    private static String getDefaultPrintable(TObject obj, Data data){
        String s = obj.toString();
        if (data.isReference()){
            Reference ref = data.asReference();
            s += "@" + ref.hashCode();
        }
        return s;
    }
}
