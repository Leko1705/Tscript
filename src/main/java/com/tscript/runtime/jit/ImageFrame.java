package com.tscript.runtime.jit;

import com.tscript.runtime.core.VirtualFrame;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.jit.tracing.Snapshot;
import com.tscript.runtime.typing.TObject;

import java.util.Map;

public class ImageFrame {

    public final VirtualFunction.VirtualMetadata metadata;
    public Snapshot snapshot = null;

    @JITSensitive public int line;
    @JITSensitive public TObject owner;
    @JITSensitive public ImageFrame next = null;
    @JITSensitive public TObject[] locals = null;  // required for loops and param-access
    public Map<String, TObject> names = null;

    public ImageFrame(VirtualFunction vfunc, TObject[] locals) {
        this.metadata = vfunc.getMetadata();
        this.line = -1;
        this.owner = vfunc.getOwner();
        this.locals = locals;
    }

    public ImageFrame(VirtualFrame frame) {
        this.metadata = frame.getMetadata();
        this.line = frame.line();
        this.owner = frame.getOwner();
    }

    public ImageFrame(VirtualFunction function, ImageFrame prev) {
        this.metadata = function.getMetadata();
        this.line = prev.line;
        this.owner = function.getOwner();
        this.next = prev;
    }

    public VirtualFrame virtualize() {
        VirtualFrame trueFrame = new VirtualFrame(metadata, owner);
        trueFrame.ip = snapshot.ip();
        trueFrame.sp = snapshot.sp();
        trueFrame.setLine(line);
        System.arraycopy(snapshot.locals(), 0, trueFrame.locals, 0, trueFrame.locals.length);
        System.arraycopy(snapshot.stack(), 0, trueFrame.stack, 0, trueFrame.stack.length);
        if (names != null) trueFrame.names.putAll(names);
        return trueFrame;
    }

}
