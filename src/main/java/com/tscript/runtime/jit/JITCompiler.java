package com.tscript.runtime.jit;

import com.tscript.runtime.jit.caching.images.ImageCache;
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
import java.util.function.Supplier;

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
    private final boolean enabled;
    private final int maxImageDepth;
    private volatile ImageCache imageCache;


    public JITCompiler() {
        this(new StandardConfiguration());
    }

    public JITCompiler(JITConfiguration configuration) {
        this.utilities = new ConcurrentHashMap<>();
        this.configuration = Objects.requireNonNull(configuration);
        this.scheduler = Objects.requireNonNull(configuration.createScheduler());
        this.enabled = !configuration.optimizationLevels().isEmpty();
        this.maxImageDepth = configuration.maxImageDepth();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        Object utility = utilities.get(key);
        if (utility != null) return (T) utility;
        return (T) utilities.computeIfAbsent(key, k -> k.create(this));
    }

    public ImageCache imageCache() {
        ImageCache cache = imageCache;
        if (cache == null) imageCache = cache = get(ImageCache.KEY);
        return cache;
    }

    public int maxImageDepth() {
        return maxImageDepth;
    }

    public CompletableFuture<NativeImage> schedule(CompileTask task) {
        CompletableFuture<NativeImage> future = new CompletableFuture<>();
        scheduler.schedule(() -> {
            try {
                future.complete(task.run(this));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public JITConfiguration getConfiguration() {
        return configuration;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void debug(Supplier<String> message) {
        if (configuration.debug()) {
            System.err.println("[jit] " + message.get());
        }
    }

    public void boot(){
    }

    public void shutdown(){
        scheduler.shutdown();
    }


    /**
     * The default configuration. Thresholds can be tuned with the system properties
     * {@code tscript.jit.threshold} (L1) and {@code tscript.jit.threshold2} (L2);
     * {@code tscript.jit.debug=true} enables diagnostic output.
     */
    private static class StandardConfiguration implements JITConfiguration {

        private final int l1Threshold = Integer.getInteger("tscript.jit.threshold", 100);
        private final int l2Threshold = Integer.getInteger("tscript.jit.threshold2", 1000);
        private final boolean debug = Boolean.getBoolean("tscript.jit.debug");
        private final List<CompilationLevel> levels = List.of(new L1(l1Threshold, !Boolean.getBoolean("tscript.jit.l1plain")), new L2(l2Threshold));

        @Override
        public List<CompilationLevel> optimizationLevels() {
            return levels;
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

        private final int inlineDepth = Integer.getInteger("tscript.jit.inlineDepth", 3);
        private final Peeling peel = switch (System.getProperty("tscript.jit.peel", "loops")) {
            case "all", "true" -> Peeling.ALL;
            case "none", "false" -> Peeling.NONE;
            default -> Peeling.LOOP_TRACES;
        };

        @Override
        public Peeling peelLoops() {
            return peel;
        }

        private final int inlineSize = Integer.getInteger("tscript.jit.inlineSize", 120);

        @Override
        public int maxInlineDepth() {
            return inlineDepth;
        }

        @Override
        public int maxInlineSize() {
            return inlineSize;
        }

        @Override
        public boolean debug() {
            return debug;
        }
    }

    private static class NoJitConfig implements JITConfiguration {

        @Override
        public List<CompilationLevel> optimizationLevels() {
            return List.of();
        }

        @Override
        public Scheduler createScheduler() {
            return r -> {};
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
