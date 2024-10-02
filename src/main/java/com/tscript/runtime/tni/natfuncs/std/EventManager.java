package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.TObject;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private static EventManager instance;

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    private EventManager() {
    }

    private Function timerHandler;
    private Function mouseMoveHandler;
    private Function keyDownHandler;

    private TThread handlerThread;
    private volatile TObject returnValue;

    public void setMouseMoveHandler(Function mouseMoveHandler) {
        this.mouseMoveHandler = mouseMoveHandler;
    }

    public void setKeyDownHandler(Function keyDownHandler) {
        this.keyDownHandler = keyDownHandler;
    }

    public void setTimerHandler(Function timerHandler) {
        this.timerHandler = timerHandler;
    }

    public TObject enterEventMode(Environment env){
        TThread thread = env.getCurrentThread();
        if (!thread.isMainThread())
            throw new AssertionError("Main-Thread only");
        handlerThread = thread;
        while (returnValue == null && handlerThread.isRunning()){
            if (timerHandler != null)
                thread.call(timerHandler, new ArrayList<>());
        }
        handlerThread = null; // avoid potential memory leak
        return returnValue;
    }

    public synchronized void quitEventMode(TObject returnValue){
        this.returnValue = returnValue;
    }

    public void fireMouseMoveEvent(){
        runEvent(mouseMoveHandler, List.of(/* impl me later */));
    }

    public void fireKeyDownEvent(){
        runEvent(keyDownHandler, List.of(/* impl me later */));
    }

    private void runEvent(Function handler, List<TObject> args){
        TThread eventThread = handlerThread.getVM().spawnThread(handler, args);
        eventThread.start();
    }

}
