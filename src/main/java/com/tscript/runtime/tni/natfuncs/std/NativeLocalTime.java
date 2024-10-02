package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.TReal;

import java.util.Calendar;
import java.util.List;

public class NativeLocalTime extends NativeFunction {

    @Override
    public String getName() {
        return "localtime";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance();
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        return new TReal((double) localtime());
    }

    private static long localtime() {
        long currentTimeMillis = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        int offset = calendar.getTimeZone().getOffset(currentTimeMillis);
        return currentTimeMillis + offset;
    }
}
