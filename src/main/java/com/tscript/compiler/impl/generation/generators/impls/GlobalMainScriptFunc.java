package com.tscript.compiler.impl.generation.generators.impls;

import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.source.utils.Location;

import java.util.List;
import java.util.Set;

public record GlobalMainScriptFunc(List<? extends StatementTree> statements) implements FunctionTree {

    @Override
    public ModifiersTree getModifiers() {
        return new ModifiersTree() {
            @Override
            public Set<Modifier> getModifiers() {
                return Set.of();
            }

            @Override
            public Location getLocation() {
                return Location.emptyLocation();
            }
        };
    }

    @Override
    public String getName() {
        return "__main__";
    }

    @Override
    public List<? extends ParameterTree> getParameters() {
        return List.of();
    }

    @Override
    public BlockTree getBody() {
        return new BlockTree() {
            @Override
            public List<? extends StatementTree> getStatements() {
                return statements;
            }

            @Override
            public Location getLocation() {
                return Location.emptyLocation();
            }
        };
    }

    @Override
    public Location getLocation() {
        return Location.emptyLocation();
    }
}