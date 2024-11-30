package com.tscript.runtime.debugger.states;

import java.util.List;

public interface VMState {

    List<ThreadState> getThreads();

    List<ObjectState> getModules();

}
