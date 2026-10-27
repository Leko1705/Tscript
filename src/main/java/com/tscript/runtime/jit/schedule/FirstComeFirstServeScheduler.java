package com.tscript.runtime.jit.schedule;

public class FirstComeFirstServeScheduler implements Scheduler {

    @Override
    public void schedule(Runnable runnable) {
        runnable.run();
    }

}
