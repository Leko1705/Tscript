package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;


public interface ContainerAccessibleObject {

     TObject readFromContainer(TThread thread, TObject key);

}
