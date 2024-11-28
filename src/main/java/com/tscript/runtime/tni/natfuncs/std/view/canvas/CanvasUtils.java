package com.tscript.runtime.tni.natfuncs.std.view.canvas;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.typing.TInteger;
import com.tscript.runtime.typing.TObject;

import java.util.List;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class CanvasUtils {
    
    public static boolean checkRectangleIntArgs(List<TObject> args, Environment env){
        return requireInteger(args.get(0), env)
                && requireInteger(args.get(1), env)
                && requireInteger(args.get(2), env)
                && requireInteger(args.get(3), env);
    }

    public static boolean checkCircleIntArgs(List<TObject> args, Environment env){
        return requireInteger(args.get(0), env)
                && requireInteger(args.get(1), env)
                && requireInteger(args.get(2), env);
    }

    public static boolean requireInteger(TObject obj, Environment env){
        if (obj.getType() != TInteger.TYPE){
            env.reportRuntimeError("<Integer> expected but got " + obj.getType());
            return false;
        }
        return true;
    }
    
}
