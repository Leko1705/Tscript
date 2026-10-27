package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.typing.TObject;

public record Snapshot(int ip, int sp, TObject[] locals, TObject[] stack) {}
