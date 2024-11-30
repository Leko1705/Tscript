package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.core.VirtualObject;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.*;

import java.util.List;
import java.util.Map;

public final class NativeDeepcopy extends NativeFunction {

    @Override
    public String getName() {
        return "deepcopy";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("value", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject value = arguments.get(0);

        try {
            return deepCopy(value);
        }
        catch (UnsupportedOperationException e) {
            env.reportRuntimeError("container must not contain itself");
            return null;
        }
        catch (ClassCastException e) {
            env.reportRuntimeError("objects are not allowed");
            return null;
        }
        catch (IllegalArgumentException e) {
            env.reportRuntimeError("callable contents are not allowed");
            return null;
        }
    }

    private TObject deepCopy(TObject value){

        if (value instanceof VirtualObject){
            throw new IllegalArgumentException();
        }

        if (value instanceof TArray a) {
            TArray newArray = new TArray();
            for (TObject content : a.getValue()){
                if (content == a)
                    throw new UnsupportedOperationException();
                newArray.getValue().add(deepCopy(content));
            }
            return newArray;
        }
        else if (value instanceof TDictionary d) {
            TDictionary newDictionary = new TDictionary();
            for (Map.Entry<TObject, TObject> entry : d.getValue().entrySet()){
                if (entry.getKey() == d)
                    throw new UnsupportedOperationException();
                if (entry.getValue() == d)
                    throw new UnsupportedOperationException();
                newDictionary.getValue().put(deepCopy(entry.getKey()), deepCopy(entry.getValue()));
            }
            return newDictionary;
        }

        return value;
    }
}
