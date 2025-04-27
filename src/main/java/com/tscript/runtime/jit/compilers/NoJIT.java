package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.concurrent.CompletableFuture;

public class NoJIT implements JITCompiler {
    
    private static final JITCache cache = new NoCache();

    @Override
    public void handle(JITTask task) {
    }

    @Override
    public JITCache getCache() {
        return cache;
    }

    private static class NoCache implements JITCache {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public NativeImage getFunctional(String context, Type[] argumentTypes) {
            return null;
        }

        @Override
        public void putFunctional(String context, Type[] argumentTypes, NativeImage image) {

        }

        @Override
        public boolean removeFunctional(String context, Type[] argumentTypes) {
            return false;
        }

        @Override
        public NativeImage getPositional(String context, int ip) {
            return null;
        }

        @Override
        public void putPositional(String context, int ip, NativeImage image) {

        }

        @Override
        public boolean removePositional(String context, int ip) {
            return false;
        }
    }
}
