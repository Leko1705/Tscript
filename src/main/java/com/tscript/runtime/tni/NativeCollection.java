package com.tscript.runtime.tni;

import com.tscript.runtime.core.ExecutionException;
import com.tscript.runtime.tni.natfuncs.std.*;
import com.tscript.runtime.tni.natfuncs.std.view.canvas.*;
import com.tscript.runtime.tni.natfuncs.std.view.turtle.*;

import java.util.HashMap;
import java.util.Map;

public class NativeCollection {

    private static final Map<String, NativeFunction> natives = new HashMap<>();

    private static void registerNativeFunction(NativeFunction nativeFunction){
        String name = nativeFunction.getName();
        if (natives.containsKey(name)){
            throw new ExecutionException(
                    "Native function already registered: "
                            + name + " (" + natives.get(name).getClass().getName() + ")");
        }
        natives.put(nativeFunction.getName(), nativeFunction);
    }


    public static NativeFunction getNativeFunction(String name){
        return natives.get(name);
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

        registerNativeFunction(new CanvasHeight());
        registerNativeFunction(new CanvasWidth());
        registerNativeFunction(new CanvasClear());
        registerNativeFunction(new CanvasLine());
        registerNativeFunction(new CanvasRect());
        registerNativeFunction(new CanvasFillRect());
        registerNativeFunction(new CanvasFrameRect());
        registerNativeFunction(new CanvasCircle());
        registerNativeFunction(new CanvasFillCircle());
        registerNativeFunction(new CanvasFrameCircle());
        registerNativeFunction(new CanvasText());

        registerNativeFunction(new TurtleTurn());
        registerNativeFunction(new TurtleMove());
        registerNativeFunction(new TurtlePen());
        registerNativeFunction(new TurtleColor());
        registerNativeFunction(new TurtleReset());

    }

}
