package com.tscript.runtime.jit.caching;

import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.typing.Type;

public class CacheDecorator implements JITCache {

    private final JITCache cache;

    public CacheDecorator(JITCache cache) {
        this.cache = cache;
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public NativeImage getFunctional(String context, Type[] argumentTypes) {
        return cache.getFunctional(context, argumentTypes);
    }

    @Override
    public void putFunctional(String context, Type[] argumentTypes, NativeImage image) {
        cache.putFunctional(context, argumentTypes, image);
    }

    @Override
    public boolean removeFunctional(String context, Type[] argumentTypes) {
        return cache.removeFunctional(context, argumentTypes);
    }

    @Override
    public NativeImage getPositional(String context, int ip) {
        return cache.getPositional(context, ip);
    }

    @Override
    public void putPositional(String context, int ip, NativeImage image) {
        cache.putPositional(context, ip, image);
    }

    @Override
    public boolean removePositional(String context, int ip) {
        return cache.removePositional(context, ip);
    }
}
