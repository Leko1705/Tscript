package com.tscript.runtime.core;

import com.tscript.projectfile.ProjectFile;
import com.tscript.runtime.VirtualMachine;
import com.tscript.runtime.debugger.DebugInterpreter;
import com.tscript.runtime.debugger.Debugger;
import com.tscript.runtime.debugger.states.ThreadState;
import com.tscript.runtime.debugger.states.VMState;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.loading.*;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.TObject;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class TscriptVM implements VirtualMachine {

    public static TscriptVM runnableInstance(File rootPath, PrintStream out, PrintStream err){
        return new TscriptVM(new File[]{rootPath}, out, err);
    }

    public static TscriptVM runnableInstance(File[] rootPaths, PrintStream out, PrintStream err){
        return new TscriptVM(rootPaths, out, err);
    }

    private volatile boolean started = false;

    private final File[] rootPaths;

    private volatile PrintStream out;
    private volatile PrintStream err;

    private volatile ModuleLoader sharedModuleLoader;
    private final Map<Long, TThread> runningThreads;
    private volatile int exitCode = 0;
    private final Set<TerminationListener> terminationListeners = new HashSet<>();
    protected ProjectFile projectFile = null;
    private Function<TThread, Interpreter> interpreterSupplier = BaseInterpreter::new;
    private Set<Integer> breakPoints = new HashSet<>();

    private TscriptVM(File[] rootPath, PrintStream out, PrintStream err){
        this.rootPaths = rootPath;
        this.out = out;
        this.err = err;
        this.sharedModuleLoader = new ModuleLoaderImpl(new BruteForcePathResolver(new DirectoryPathResolver(new FileHierarchyPathResolver())));
        this.runningThreads = new ConcurrentHashMap<>();
    }

    public int execute(String moduleName){
        checkNotRunning();

        started = true;

        Module module;
        try {
            module = sharedModuleLoader.loadModule(rootPaths, moduleName.split("[.]"));
            if (module == null)
                throw new ModuleLoadingException(new NullPointerException());
        }
        catch (ModuleLoadingException ex){
            err.println(ex.getMessage());
            return -1;
        }

        // run the main thread
        Callable entryPoint = module.getEntryPoint();
        TThread thread = spawnThread(entryPoint, List.of());
        thread.begin();

        // wait for daemon threads
        while (!runningThreads.isEmpty()){
            TThread toWaitForThread = runningThreads.values().iterator().next();

            try {
                toWaitForThread.join();
                runningThreads.remove(toWaitForThread.getId());
            }
            catch (InterruptedException ex){
                throw new RuntimeException(ex);
            }
        }

        for (TerminationListener listener : terminationListeners){
            listener.onTermination(this);
        }

        return exitCode;
    }

    public ModuleLoader getSharedModuleLoader() {
        return sharedModuleLoader;
    }

    public void setSharedModuleLoader(ModuleLoader sharedModuleLoader) {
        checkNotRunning();
        this.sharedModuleLoader = sharedModuleLoader;
    }

    public void setBuildFile(ProjectFile projectFile) {
        checkNotRunning();
        this.projectFile = projectFile;
    }

    public void setDebugger(Debugger debugger) {
        checkNotRunning();
        if (debugger == null){
            interpreterSupplier = BaseInterpreter::new;
        }
        else {
            interpreterSupplier =
                    t -> new DebugInterpreter(new BaseInterpreter(t), debugger, breakPoints);
        }
    }

    public void setBreakPoints(Set<Integer> breakPoints) {
        Objects.requireNonNull(breakPoints);
        this.breakPoints = breakPoints;
    }

    public Set<Integer> getBreakPoints() {
        return breakPoints;
    }

    public void addTerminationListener(TerminationListener listener){
        terminationListeners.add(listener);
    }

    public void removeTerminationListener(TerminationListener listener){
        terminationListeners.remove(listener);
    }

    public synchronized void exit(int status){
        if (!runningThreads.isEmpty())
            exitCode = status;
        for(TThread thread : runningThreads.values()){
            thread.running = false;
        }
        runningThreads.clear();
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void setErr(PrintStream err) {
        this.err = err;
    }

    public TThread spawnThread(Callable callable, List<TObject> arguments){
        TThread thread = new TThread(this, callable, arguments, interpreterSupplier);
        runningThreads.put(thread.getId(), thread);
        return thread;
    }

    protected void removeThread(Long id){
        runningThreads.remove(id);
    }

    public File[] getRootPaths() {
        return rootPaths;
    }

    public Collection<TThread> getThreads(){
        return runningThreads.values();
    }


    private void checkNotRunning(){
        if (started)
            throw new IllegalStateException("vm already running");
    }

}
