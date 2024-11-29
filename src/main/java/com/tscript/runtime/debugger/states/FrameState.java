package com.tscript.runtime.debugger.states;

import java.util.List;

public interface FrameState {

    String getName();

    List<ObjectState> getLocals();

    List<ObjectState> getStack();
}
