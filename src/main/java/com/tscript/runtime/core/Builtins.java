package com.tscript.runtime.core;

import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.tni.natfuncs.std.*;
import com.tscript.runtime.typing.*;

import java.util.*;

public class Builtins {

    private static final Map<String, Integer> indexMap = new HashMap<>();
    private static final List<TObject> builtins = new ArrayList<>();

    private static void registerBuiltin(String name, TObject builtin) {
        indexMap.put(name, indexMap.size());
        builtins.add(builtin);
    }

    private static void registerBuiltin(Callable builtin) {
        registerBuiltin(builtin.getName(), builtin);
    }

    @JITSensitive
    public static TObject load(int index){
        return builtins.get(index);
    }

    public static int indexOf(String name){
        return indexMap.getOrDefault(name, -1);
    }

    public static boolean hasBuiltin(TObject o) {
        return builtins.contains(o);
    }

    static {
        registerBuiltin(Type.TYPE);
        registerBuiltin(Null.TYPE);
        registerBuiltin(TInteger.TYPE);
        registerBuiltin(TReal.TYPE);
        registerBuiltin(TBoolean.TYPE);
        registerBuiltin(TString.TYPE);
        registerBuiltin(Function.TYPE);
        registerBuiltin(Range.TYPE);
        registerBuiltin(TArray.TYPE);
        registerBuiltin(TDictionary.TYPE);

        registerBuiltin(new NativePrint());
        registerBuiltin(new NativeExit());
        registerBuiltin(new NativeAlert());
        registerBuiltin(new NativeAssert());
        registerBuiltin(new NativeConfirm());
        registerBuiltin(new NativeDeepcopy());
        registerBuiltin(new NativeError());
        registerBuiltin(new NativeExists());
        registerBuiltin(new NativeListKeys());
        registerBuiltin(new NativeLocalTime());
        registerBuiltin(new NativePrompt());
        registerBuiltin(new NativeSame());
        registerBuiltin(new NativeTime());
        registerBuiltin(new NativeVersion());
        registerBuiltin(new NativeWait());
        registerBuiltin(new NativeSetEventHandler());
        registerBuiltin(new NativeEnterEventMode());
        registerBuiltin(new NativeQuitEventMode());
    }


}
