package com.tscript.runtime.type;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.core.TThread;

public interface ContainerWriteable extends TObject {

    boolean writeToContainer(TThread thread, Data key, Data value);

}
