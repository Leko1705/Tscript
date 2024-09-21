package com.tscript.tscriptc.generation.generators;

import com.tscript.tscriptc.analyze.scoping.Scope;
import com.tscript.tscriptc.generation.compiled.CompiledFile;
import com.tscript.tscriptc.generation.compiled.GlobalVariable;
import com.tscript.tscriptc.generation.compiled.instruction.*;
import com.tscript.tscriptc.generation.generators.impls.CompFile;
import com.tscript.tscriptc.generation.generators.impls.GlobalMainScriptFunc;
import com.tscript.tscriptc.generation.generators.impls.PoolPutter;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.SimpleTreeVisitor;

import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.List;

public class FileGenerator extends SimpleTreeVisitor<Scope, Void> {


    private final CompFile file = new CompFile();
    private final Context context = new Context(file);

    private final List<Instruction> preloadInstructions = new ArrayList<>();

    private FunctionTree mainFunction = null;


    public CompiledFile getCompiled(){
        return context.getFile();
    }

    @Override
    public Void visitRoot(RootTree node, Scope scope) {

        file.moduleName = "test";

        GlobalRegistry reg = new GlobalRegistry(scope);
        for (DefinitionTree definitionTree : node.getDefinitions()) {
            definitionTree.accept(reg, file.getGlobalVariables());
        }
        for (StatementTree statement : node.getStatements()) {
            statement.accept(reg, file.getGlobalVariables());
        }

        for (DefinitionTree def : node.getDefinitions()) {
            def.accept(this, scope);
        }

        FunctionGenerator generator;
        if (mainFunction != null) {
            generator = new FunctionGenerator(context, mainFunction);
            generator.stackGrows();
            generator.stackShrinks();
            generator.addPreInstructions(preloadInstructions);
            generator.handle(mainFunction, scope);
        }
        else {
            FunctionTree mainFunc = new GlobalMainScriptFunc(node.getStatements());

            generator = new FunctionGenerator(context, mainFunc);
            generator.stackGrows();
            generator.stackShrinks();
            generator.addPreInstructions(preloadInstructions);
            generator.handle(mainFunc, scope);
        }

        file.entryPoint = generator.func.getIndex();

        return null;
    }


    @Override
    public Void visitFunction(FunctionTree node, Scope scope) {
        if (node.getName().equals("__main__")){
            // handle main function at the end when all
            // global loadings are detected and generated
            mainFunction = node;
            return null;
        }

        if (node.getModifiers().getModifiers().contains(Modifier.NATIVE)){
            int poolAddr = PoolPutter.putUtf8(context, node.getName());
            preloadInstructions.add(new LoadNative(poolAddr));
            preloadInstructions.add(new StoreGlobal(file.getGlobalIndex(node.getName())));
        }
        else {
            int index = FunctionGenerator.generate(context, node, scope.getChildScope(node));
            preloadInstructions.add(new LoadVirtual(index));
            preloadInstructions.add(new StoreGlobal(file.getGlobalIndex(node.getName())));
        }
        return null;
    }

}
