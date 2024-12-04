package com.tscript.runtime.jit;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.tracing.ImageFrame;
import com.tscript.runtime.typing.TObject;

public interface NativeImage {

    TObject run(TThread thread, ImageFrame frame);
}
