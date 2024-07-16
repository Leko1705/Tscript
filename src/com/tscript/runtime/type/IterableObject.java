package com.tscript.runtime.type;

import com.tscript.runtime.core.Data;

import java.util.Iterator;

public interface IterableObject extends TObject {


    IteratorObject iterator();

}
