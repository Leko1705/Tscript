package com.tscript.runtime.debugger.states;

import java.util.Map;

public interface ObjectState {

    Map<String, ObjectState> getMembers();

}
