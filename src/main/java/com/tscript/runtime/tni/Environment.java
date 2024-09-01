package com.tscript.runtime.tni;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.TString;

import java.util.List;

public interface Environment {

    TThread getCurrentThread();

    void reportRuntimeError(TObject message);

    default void reportRuntimeError(String message){
        reportRuntimeError(new TString(message));
    }

    TObject call(Callable called, List<TObject> arguments);

}
