package runtime.jit.compile;

import runtime.jit.table.LookUpTable;

public interface Task {

    enum Kind {
        METHOD,
        LOOP,
    }

    Kind getKind();

    void handle(LookUpTable table);

}
