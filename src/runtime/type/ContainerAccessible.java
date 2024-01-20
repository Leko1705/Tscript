package runtime.type;

import runtime.core.Data;
import runtime.core.TThread;

public interface ContainerAccessible extends TObject {

    Data readFromContainer(TThread thread, Data key);


}
