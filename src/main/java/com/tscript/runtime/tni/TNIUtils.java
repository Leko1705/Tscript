package com.tscript.runtime.tni;

import com.tscript.runtime.core.Frame;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.VirtualObject;
import com.tscript.runtime.typing.*;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TNIUtils {

    public static String toString(Environment env, TObject object) {
        Member definedFunction = lookupStrMethod(object);
        if (definedFunction == null)
            return object.getDisplayName();
        else
            return getDefinedPrintable((TThread) env, object, definedFunction);
    }

    private static Member lookupStrMethod(TObject object) {
        Member definedFunction = object.loadMember("__str__");
        if (definedFunction != null) return definedFunction;
        if (!(object instanceof VirtualObject vobj)) return null;
        return lookupStrMethod(vobj.getSuper());
    }

    private static String getDefinedPrintable(TThread thread,
                                              TObject object,
                                              Member definedFunction){
        TObject candidate = definedFunction.get();
        if (candidate instanceof Function c && c.getParameters(thread).count() == 0){
            TObject toPrint = thread.call(c, List.of());
            if (toPrint == null) return null;
            if (toPrint == c.getOwner()) {
                thread.reportRuntimeError("internalStackOverflowError in print: unable to print owner recursively");
                return null;
            }
            return toString(thread, toPrint);
        }
        else {
            return object.getDisplayName();
        }
    }


    public static boolean areEqual(TObject o1, TObject o2) {
        return o1.equals(o2);
    }

    public static boolean isTrue(TObject object) {
        if (object instanceof TBoolean i && !i.getValue()) return false;
        if (object == Null.INSTANCE) return false;
        if (object instanceof TInteger i && i.getValue() == 0) return false;
        if (object instanceof TString s && s.getValue().isEmpty()) return false;
        return !(object instanceof TArray a) || !a.getValue().isEmpty();
    }

    public static Member searchMember(TObject object, String name) {
        while (object != null){
            Member member = object.loadMember(name);
            if (member != null)
                return member;
            object = object.getSuper();
        }
        return null;
    }

    public static TObject evalNativeContext(
            TThread thread,
            Callable callable,
            Supplier<TObject> callback
    ) {

        thread.frameStack.push(Frame.createNativeFrame(callable));
        TObject result = callback.get();
        if (result == null) return null;
        thread.frameStack.pop();
        return result;
    }

}
