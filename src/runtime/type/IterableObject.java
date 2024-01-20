package runtime.type;

import runtime.core.Data;

import java.util.Iterator;

public interface IterableObject extends TObject {


    IteratorObject iterator();

}
