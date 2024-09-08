package com.tscript.tscriptc.generation.generators;

import com.tscript.tscriptc.analyze.scoping.Scope;
import com.tscript.tscriptc.generation.compiled.CompiledClass;
import com.tscript.tscriptc.generation.compiled.CompiledFile;
import com.tscript.tscriptc.generation.compiled.CompiledFunction;
import com.tscript.tscriptc.generation.compiled.GlobalVariable;
import com.tscript.tscriptc.generation.compiled.pool.ConstantPool;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Location;
import com.tscript.tscriptc.utils.SimpleTreeVisitor;
import com.tscript.tscriptc.utils.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileGenerator extends SimpleTreeVisitor<Scope, Void> {


    private final CompFile file = new CompFile();


    public CompiledFile getCompiled(){
        return file;
    }

    @Override
    public Void visitRoot(RootTree node, Scope scope) {

        for (DefinitionTree def : node.getDefinitions()) {
            def.accept(this, scope);
        }

        if (file.entryPoint != -1)
            return null;

        FunctionTree mainFunc = new ScriptMain(node.getStatements());
        visitFunction(mainFunc, scope);

        return null;
    }


    @Override
    public Void visitFunction(FunctionTree node, Scope scope) {
        return super.visitFunction(node, scope);
    }

    private static class CompFile implements CompiledFile {

        private String moduleName;
        private int entryPoint;
        private ConstantPool pool;
        private Set<CompiledFunction> functions;
        private Set<CompiledClass> classes;

        @Override
        public String getModuleName() {
            return moduleName;
        }

        @Override
        public Version getVersion() {
            return new Version(0, 0);
        }

        @Override
        public int getEntryPoint() {
            return entryPoint;
        }

        @Override
        public List<GlobalVariable> getGlobalVariables() {
            return List.of();
        }

        @Override
        public ConstantPool getConstantPool() {
            return pool;
        }

        @Override
        public List<CompiledFunction> getFunctions() {
            return new ArrayList<>(functions);
        }

        @Override
        public List<CompiledClass> getClasses() {
            return new ArrayList<>(classes);
        }
    }


    private record ScriptMain(List<? extends StatementTree> statements) implements FunctionTree {

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
}
