package com.tscript.runtime.debugger.states;

import java.util.List;

public interface FrameState {

    String getName();

    int getLineNumber();

    List<ObjectState> getLocals();

    List<ObjectState> getStack();
}
