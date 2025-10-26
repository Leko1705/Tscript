package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.CacheEntry;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.compilers.tasks.JBCCompileTask;
import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.typing.Type;

import java.util.function.Consumer;

public class TracerConfiguration {

    private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];


    public static TracerConfiguration createLoopConfig(String context, int initialFunctionStackSize, int jumpBackInstructionAddress) {
        return new TracerConfiguration(context, initialFunctionStackSize, EMPTY_TYPE_ARRAY, -1, jumpBackInstructionAddress);
    }

    public static TracerConfiguration createFunctionConfig(String context, int initialFunctionStackSize, Type[] argTypes, int paramCount) {
        return new TracerConfiguration(context, initialFunctionStackSize, argTypes, paramCount, -1);
    }


    private final String context;
    private final int initialFunctionStackSize;

    private int params;
    private final Type[] argTypes;

    private final int jumpBackInstructionAddress;


    private TracerConfiguration(String context, int initialFunctionStackSize, Type[] argTypes, int params, int jumpBackInstructionAddress) {
        this.context = context;
        this.initialFunctionStackSize = initialFunctionStackSize;
        this.argTypes = argTypes;
        this.params = params;
        this.jumpBackInstructionAddress = jumpBackInstructionAddress;
    }

    public int getInitialFunctionStackSize() {
        return initialFunctionStackSize;
    }

    public int getJumpBackInstructionAddress() {
        return jumpBackInstructionAddress;
    }

    public boolean isParameterStorage() {
        if (params <= 0) return false;
        params--;
        return true;
    }

    public JITTask completeTrace(TThread thread, IRBlock block) {
        JITCache cacheV2 = thread.getVM().getJitCompiler().getCache();

        Consumer<NativeImage> compilationCompletionObserver = image -> {
            CacheEntry entry = cacheV2.get(context);
            entry.image = image;
        };

        return new JBCCompileTask(block, argTypes, compilationCompletionObserver);
    }

}
