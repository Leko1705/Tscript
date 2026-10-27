package com.tscript.runtime.jit;

import com.tscript.runtime.jit.comp.emit.Emitter;
import com.tscript.runtime.jit.comp.emit.JBCEmitter;
import com.tscript.runtime.jit.comp.levels.L1;
import com.tscript.runtime.jit.comp.levels.L2;
import com.tscript.runtime.jit.schedule.FirstComeFirstServeScheduler;
import com.tscript.runtime.jit.schedule.Scheduler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class JITCompiler {

    public static JITCompiler disabled(){
        return new JITCompiler(new NoJitConfig());
    }


    public interface Key<T> {
        T create(JITCompiler compiler);
    }

    private final JITConfiguration configuration;
    private final Map<Key<?>, Object> utilities;
    private final Scheduler scheduler;


    public JITCompiler() {
        this(new StandardConfiguration());
    }

    public JITCompiler(JITConfiguration configuration) {
        this.utilities = new ConcurrentHashMap<>();
        this.configuration = Objects.requireNonNull(configuration);
        this.scheduler = Objects.requireNonNull(configuration.createScheduler());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) utilities.computeIfAbsent(key, k -> k.create(this));
    }

    public CompletableFuture<NativeImage> schedule(CompileTask task) {
        CompletableFuture<NativeImage> future = new CompletableFuture<>();
        scheduler.schedule(() -> future.complete(task.run(this)));
        return future;
    }

    public JITConfiguration getConfiguration() {
        return configuration;
    }

    public void boot(){
    }

    public void shutdown(){
        scheduler.shutdown();
    }


    private static class StandardConfiguration implements JITConfiguration {

        @Override
        public List<CompilationLevel> optimizationLevels() {
            return List.of(new L1(100), new L2(100));
        }

        @Override
        public Scheduler createScheduler() {
            return new FirstComeFirstServeScheduler();
        }

        @Override
        public Emitter emitter() {
            return new JBCEmitter();
        }

        @Override
        public int specializationLimit() {
            return 5;
        }
    }

    private static class NoJitConfig implements JITConfiguration {

        @Override
        public List<CompilationLevel> optimizationLevels() {
            return List.of();
        }

        @Override
        public Scheduler createScheduler() {
            return r -> new CompletableFuture<>();
        }

        @Override
        public Emitter emitter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int specializationLimit() {
            return 0;
        }
    }

}
