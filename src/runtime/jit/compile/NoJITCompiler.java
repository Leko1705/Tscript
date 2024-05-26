package runtime.jit.compile;

import runtime.jit.table.LookUpTable;
import runtime.jit.table.LookUpTableImpl;
import runtime.type.Callable;

class NoJITCompiler implements JITCompiler {

    private static LookUpTable lookupTable;

    public NoJITCompiler() {
        if (lookupTable == null) {
            lookupTable = new LookUpTableImpl();
        }
    }

    @Override
    public LookUpTable getLookUpTable() {
        return lookupTable;
    }

    @Override
    public void handle(Task task) { }

    private static class NoLookUpTable implements LookUpTable {
        @Override public boolean isOptimized(Callable callable) { return false; }
        @Override public Callable getOptimized(Callable callable) { return null; }
        @Override public void addOptimized(Callable callable, Callable optimized) { }
        @Override public void removeOptimized(Callable callable) { }
        @Override public void replaceOptimized(Callable callable, Callable optimized) { }
    }
}
