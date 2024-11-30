package com.tscript.runtime.stroage;

import com.tscript.runtime.core.VirtualFunction;

public interface FunctionArea {

    VirtualFunction loadFunction(int index, Module module);

}
