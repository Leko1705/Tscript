package com.tscript.runtime;

import com.tscript.runtime.core.TscriptVM;

import java.io.File;
import java.io.PrintStream;

public class VMFactory {

    private VMFactory() {
    }

    public static TscriptVM newRunnableTscriptVM(File rootPath, PrintStream out, PrintStream err) {
        return TscriptVM.runnableInstance(rootPath, out, err);
    }

    public static TscriptVM newRunnableTscriptVM(File[] rootPath, PrintStream out, PrintStream err) {
        return TscriptVM.runnableInstance(rootPath, out, err);
    }
}
