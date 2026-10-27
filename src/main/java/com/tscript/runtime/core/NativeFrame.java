package com.tscript.runtime.core;

import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;

public class NativeFrame implements Frame {

    private final Callable.Metadata metadata;
    private final TObject owner;

    public NativeFrame(Callable callable, TObject owner) {
        this.metadata = callable.getMetadata();
        this.owner = owner;
    }

    public NativeFrame(Callable callable) {
        this(callable, null);
    }

    @Override
    public TObject getOwner() {
        return owner;
    }

    @Override
    public Callable.Metadata getMetadata() {
        return metadata;
    }
}
