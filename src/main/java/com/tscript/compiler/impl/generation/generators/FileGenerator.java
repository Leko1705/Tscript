package com.tscript.compiler.impl.generation.generators;

import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.impl.generation.compiled.instruction.Instruction;
import com.tscript.compiler.impl.generation.compiled.instruction.LoadNative;
import com.tscript.compiler.impl.generation.compiled.instruction.LoadVirtual;
import com.tscript.compiler.impl.generation.compiled.instruction.StoreGlobal;
import com.tscript.compiler.impl.generation.generators.impls.CompFile;
import com.tscript.compiler.impl.generation.generators.impls.GlobalMainScriptFunc;
import com.tscript.compiler.impl.generation.generators.impls.PoolPutter;
import com.tscript.compiler.impl.utils.Scope;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.source.utils.SimpleTreeVisitor;

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

        GlobalRegistry reg = new GlobalRegistry(((TCTree.TCRootTree)node).scope);
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

        if (node.getModifiers().getFlags().contains(Modifier.NATIVE)){
            int poolAddr = PoolPutter.putUtf8(context, node.getName());
            preloadInstructions.add(new LoadNative(poolAddr));
            preloadInstructions.add(new StoreGlobal(file.getGlobalIndex(node.getName())));
        }
        else {
            int index = FunctionGenerator.generate(context, node, ((TCTree.TCFunctionTree)node).sym.subScope);
            preloadInstructions.add(new LoadVirtual(index));
            preloadInstructions.add(new StoreGlobal(file.getGlobalIndex(node.getName())));
        }
        return null;
    }

}
