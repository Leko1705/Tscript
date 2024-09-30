package com.tscript.compiler.impl.generation.generators2;

import com.tscript.compiler.impl.utils.Scope;
import com.tscript.compiler.impl.utils.Symbol;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.utils.Location;

import java.util.List;
import java.util.Set;

public class ScriptMainFunc extends TCTree.TCFunctionTree {

    public ScriptMainFunc(List<? extends TCStatementTree> statements) {
        super(
                Location.emptyLocation(),
                "__main__",
                new TCModifiersTree(Location.emptyLocation(), Set.of()),
                List.of(),
                new TCBlockTree(Location.emptyLocation(), statements));

        sym = new Symbol.FunctionSymbol(
                "__main__",
                Set.of(),
                new Scope.GlobalScope(),
                -1,
                Location.emptyLocation());
    }

}
