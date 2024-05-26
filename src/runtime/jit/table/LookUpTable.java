package runtime.jit.table;

import runtime.type.Callable;

public interface LookUpTable {

    boolean isOptimized(Callable callable);

    Callable getOptimized(Callable callable);

    void addOptimized(Callable callable, Callable optimized);

    void removeOptimized(Callable callable);

    void replaceOptimized(Callable callable, Callable optimized);

}
