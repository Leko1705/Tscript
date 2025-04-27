package com.tscript.runtime.jit.caching;

import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.typing.Type;

public interface JITCache {

    int size();

    NativeImage getFunctional(String context, Type[] argumentTypes);

    void putFunctional(String context, Type[] argumentTypes, NativeImage image);

    boolean removeFunctional(String context, Type[] argumentTypes);

    NativeImage getPositional(String context, int ip);

    void putPositional(String context, int ip, NativeImage image);

    boolean removePositional(String context, int ip);

}
