package runtime.jit.compile;

import runtime.jit.table.LookUpTable;
import runtime.jit.table.LookUpTableImpl;

class MethodJIT implements JITCompiler {

    private final LookUpTable lookupTable = new LookUpTableImpl();

    @Override
    public LookUpTable getLookUpTable() {
        return lookupTable;
    }

    @Override
    public void handle(Task task) {
        if (task.getKind() != Task.Kind.METHOD) return;
        LookUpTable lookupTable = getLookUpTable();
        task.handle(lookupTable);
    }

}
