package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.*;

import java.util.LinkedHashMap;
import java.util.List;

public class NativeVersion extends NativeFunction {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance();
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        return new TDictionary(getVersionData());
    }

    private static LinkedHashMap<TObject, TObject> getVersionData(){
        LinkedHashMap<TObject, TObject> data = new LinkedHashMap<>();

        data.put(new TString("type"), Null.INSTANCE);
        data.put(new TString("major"), new TInteger(1));
        data.put(new TString("minor"), new TInteger(2));
        data.put(new TString("patch"), new TInteger(2));

        data.put(new TString("day"), new TInteger(0));
        data.put(new TString("month"), new TInteger(0));
        data.put(new TString("year"), new TInteger(2024));
        data.put(new TString("full"), new TString("TScript version 8 - released 0.0.2024 - (C) Lennart Koehler 2024-2024"));

        return data;
    }
}
