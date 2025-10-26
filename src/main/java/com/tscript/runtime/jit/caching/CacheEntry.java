package com.tscript.runtime.jit.caching;

import com.tscript.runtime.jit.NativeImage;

public class CacheEntry {
    public NativeImage image;
    public int hotness = 0;
}
