package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;

public interface ContainerWriteableObject {

    boolean writeToContainer(TThread thread, TObject key, TObject value);

}
