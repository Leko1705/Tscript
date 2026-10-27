package com.tscript.runtime.core;

import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;

public interface Frame {

    TObject getOwner();

    Callable.Metadata getMetadata();

}
