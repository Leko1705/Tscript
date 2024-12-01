package com.tscript.runtime.jit.lower;

import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;

public interface Generator {

    NativeImage generate(Node graph);

}
