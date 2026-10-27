package com.tscript.runtime.jit.schedule;

public interface Scheduler {

    void schedule(Runnable runnable);


    default void shutdown() {
    }

}
