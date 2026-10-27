package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.typing.TObject;

public record SnapshotShape(int ip, int sp, int[] localsMapping, int[] stackMapping) {

    public Snapshot inflate(TObject[] locals, TObject[] stack) {
        return new Snapshot(ip, sp, locals, stack);
    }

}
