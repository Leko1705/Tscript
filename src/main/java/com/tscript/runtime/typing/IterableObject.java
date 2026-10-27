package com.tscript.runtime.typing;

public interface IterableObject extends TObject, Iterable<TObject> {

    IteratorObject iterator();

}
