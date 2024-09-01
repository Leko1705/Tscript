package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;

public interface ContainerWriteableObject {

    void writeToContainer(TThread thread, TObject key, TObject value);

}
