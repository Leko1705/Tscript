package com.tscript.runtime.debugger.states;

import java.util.List;

public interface ThreadState {

    long getId();

    List<FrameState> getFrames();

}
