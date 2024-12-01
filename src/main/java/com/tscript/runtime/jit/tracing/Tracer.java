package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.Interpreter;
import com.tscript.runtime.core.InterpreterDecorator;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.instructions.Instruction;
import com.tscript.runtime.jit.instructions.PushNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Tracer extends InterpreterDecorator {

    private static final int MAX_TRACE_LIMIT = 1000;

    private final Profiler profiler;
    private final Profiler.Value value;
    private final int callStackDepth;
    private final List<Instruction> trace;

    public Tracer(Interpreter interpreter, Profiler profiler, int callStackDepth, Profiler.Value value) {
        super(interpreter);
        this.profiler = profiler;
        this.callStackDepth = callStackDepth;
        this.value = value;
        trace = new ArrayList<>();
    }

    @Override
    public void pushNull() {
        super.pushNull();
        extend(PushNull.INSTANCE);
    }

    private void extend(Instruction instruction){
        TThread thread = getCurrentThread();

        if (trace.size() + 1 >= MAX_TRACE_LIMIT || thread.frameStack.size() < callStackDepth){
            thread.setInterpreter(profiler);
            return;
        }

        if (thread.frameStack.size() == callStackDepth){
            JITCompiler jit = thread.getVM().getJitCompiler();
            CompletableFuture<NativeImage> future = jit.compile(trace);
            future.thenAccept(img -> value.image = img);
            return;
        }

        trace.add(instruction);
    }

}
