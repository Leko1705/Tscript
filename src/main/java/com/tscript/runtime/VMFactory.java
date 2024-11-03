package com.tscript.runtime;

import com.tscript.runtime.core.TscriptVM;

import java.io.File;
import java.io.PrintStream;

public class VMFactory {

    private VMFactory() {
    }

    public static VirtualMachine newRunnableTscriptVM(File rootPath, PrintStream out, PrintStream err) {
        return TscriptVM.runnableInstance(rootPath, out, err);
    }

    public static VirtualMachine newRunnableTscriptVM(File[] rootPath, PrintStream out, PrintStream err) {
        return TscriptVM.runnableInstance(rootPath, out, err);
    }
}
