package com.tscript.runtime.typing;

import java.util.Iterator;

public interface IteratorObject extends TObject, Iterator<TObject> {

    TObject next();

    boolean hasNext();

}
