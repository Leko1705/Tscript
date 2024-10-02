package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.*;

import java.util.List;

public class NativeSetEventHandler extends NativeFunction {

    @Override
    public String getName() {
        return "setEventHandler";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance()
                .add("event", null)
                .add("handler", null);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        TObject event = arguments.get(0);

        if (!(event instanceof TString s)){
            env.reportRuntimeError("event must be type of String");
            return null;
        }

        TObject handler = arguments.get(1);

        if (!(handler instanceof Function) && handler != Null.INSTANCE){
            env.reportRuntimeError("handler must be null or a function");
            return null;
        }

        String eventEncoding = s.getValue();
        Function func = handler == Null.INSTANCE ? null : (Function) handler;

        switch (eventEncoding) {
            case "timer" -> EventManager.getInstance().setTimerHandler(func);
            case "canvas.mousemove" -> EventManager.getInstance().setMouseMoveHandler(func);
            case "canvas.mousedown" -> EventManager.getInstance().setKeyDownHandler(func);
            default -> {
                env.reportRuntimeError("invalid event-type: " + eventEncoding);
                return null;
            }
        }

        return Null.INSTANCE;
    }
}
