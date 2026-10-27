package com.tscript.runtime.jit;

import com.tscript.runtime.jit.comp.emit.Emitter;
import com.tscript.runtime.jit.schedule.Scheduler;

import java.util.List;

public interface JITConfiguration {

    List<CompilationLevel> optimizationLevels();

    Scheduler createScheduler();

    Emitter emitter();

    /**
     * @return the maximum number of type specializations per function before it is blacklisted
     */
    int specializationLimit();

    /**
     * @return the maximum number of instructions recorded in one trace
     */
    default int traceLengthLimit() {
        return 1500;
    }

    /**
     * @return how often an image may misspeculate before it is invalidated
     */
    default int deoptLimit() {
        return 100;
    }

    /**
     * @return how often recording/compiling may fail (or an image may be invalidated) before an entry is disabled
     */
    default int failureLimit() {
        return 3;
    }

    /**
     * @return the maximum nesting depth of native image invocations (limits the java stack usage)
     */
    default int maxImageDepth() {
        return 400;
    }

    /**
     * @return true if the jit should print diagnostic output (graphs, generated code, failures)
     */
    default boolean debug() {
        return false;
    }

}
