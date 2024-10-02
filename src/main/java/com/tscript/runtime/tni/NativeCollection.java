package com.tscript.runtime.tni;

import com.tscript.runtime.core.ExecutionException;
import com.tscript.runtime.tni.natfuncs.std.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NativeCollection {


    private static final Map<String, NativeFunction> natives = new HashMap<>();

    private static void registerNativeFunction(NativeFunction nativeFunction){
        if (natives.containsKey(nativeFunction.getName())){
            throw new ExecutionException("Native function already registered: " + nativeFunction.getName());
        }
        natives.put(nativeFunction.getName(), nativeFunction);
    }


    public static NativeFunction getNativeFunction(String name){
        return natives.get(name);
    }

    public static Collection<NativeFunction> getNativeFunctions(){
        return natives.values();
    }

    static {
        registerNativeFunction(new NativePrint());
        registerNativeFunction(new NativeExit());
        registerNativeFunction(new NativeAlert());
        registerNativeFunction(new NativeAssert());
        registerNativeFunction(new NativeConfirm());
        registerNativeFunction(new NativeDeepcopy());
        registerNativeFunction(new NativeError());
        registerNativeFunction(new NativeExists());
        registerNativeFunction(new NativeListKeys());
        //registerNativeFunction(new NativeLoad());
        registerNativeFunction(new NativeLocalTime());
        registerNativeFunction(new NativePrompt());
        registerNativeFunction(new NativeSame());
        //registerNativeFunction(new NativeSave());
        registerNativeFunction(new NativeTime());
        registerNativeFunction(new NativeVersion());
        registerNativeFunction(new NativeWait());

        registerNativeFunction(new NativeSetEventHandler());
        registerNativeFunction(new NativeEnterEventMode());
        registerNativeFunction(new NativeQuitEventMode());
    }

}
