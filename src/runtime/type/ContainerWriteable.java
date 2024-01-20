package runtime.type;

import runtime.core.Data;
import runtime.core.TThread;

public interface ContainerWriteable extends TObject {

    boolean writeToContainer(TThread thread, Data key, Data value);

}
