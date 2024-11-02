package com.tscript.runtime.tni;

import com.tscript.runtime.core.ExecutionException;
import com.tscript.runtime.tni.natfuncs.api4j.DropJavaClass;
import com.tscript.runtime.tni.natfuncs.api4j.IsJavaType;
import com.tscript.runtime.tni.natfuncs.api4j.LoadJavaClass;
import com.tscript.runtime.tni.natfuncs.std.*;
import com.tscript.runtime.tni.natfuncs.std.threading.NativeCurrentThread;
import com.tscript.runtime.tni.natfuncs.std.threading.ThreadSpawnNative;
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

        registerNativeFunction(new ThreadSpawnNative());
        registerNativeFunction(new NativeCurrentThread());

        registerNativeFunction(new LoadJavaClass());
        registerNativeFunction(new DropJavaClass());
        registerNativeFunction(new IsJavaType());
    }

}
