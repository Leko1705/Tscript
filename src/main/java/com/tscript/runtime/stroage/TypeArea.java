package com.tscript.runtime.stroage;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.VirtualType;
import com.tscript.runtime.typing.Type;

public interface TypeArea {

    VirtualType loadType(TThread thread, int index);

}
