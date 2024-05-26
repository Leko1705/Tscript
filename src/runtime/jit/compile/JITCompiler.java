package runtime.jit.compile;

import runtime.jit.table.LookUpTable;

public interface JITCompiler {

    LookUpTable getLookUpTable();

    void handle(Task task);

    default void close() { }
}
