package com.tscript.runtime.type;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.core.TThread;

public interface ContainerAccessible extends TObject {

    Data readFromContainer(TThread thread, Data key);


}
