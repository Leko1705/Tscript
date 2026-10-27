package com.tscript.runtime.jit;

import com.tscript.runtime.jit.comp.emit.Emitter;
import com.tscript.runtime.jit.schedule.Scheduler;

import java.util.List;

public interface JITConfiguration {

    List<CompilationLevel> optimizationLevels();

    Scheduler createScheduler();

    Emitter emitter();

    int specializationLimit();

}
