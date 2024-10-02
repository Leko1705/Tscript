package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.generation.gen.adapter.NamespaceClass;
import com.tscript.compiler.impl.generation.gen.adapter.ScriptMainFunc;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.impl.utils.TCTreeScanner;
import com.tscript.compiler.source.tree.DefinitionTree;
import com.tscript.compiler.source.tree.Modifier;
import com.tscript.compiler.source.tree.StatementTree;

import java.util.ArrayList;
import java.util.List;

public class FileGenerator extends TCTreeScanner<Void, Void> {

    private final CompFile file = new CompFile();
    private final Context context = new Context(file);

    private final List<Instruction> preloadInstructions = new ArrayList<>();

    private TCTree.TCFunctionTree mainFunction = null;

    public CompFile getCompiled(){
        return file;
    }

    @Override
    public Void visitRoot(TCTree.TCRootTree node, Void unused) {
        file.moduleName = "test";

        GlobalRegistry reg = new GlobalRegistry();
        for (DefinitionTree definitionTree : node.getDefinitions()) {
            definitionTree.accept(reg, file.getGlobalVariables());
        }
        for (StatementTree statement : node.getStatements()) {
            statement.accept(reg, file.getGlobalVariables());
        }

        for (TCTree.TCDefinitionTree def : node.definitions) {
            def.accept(this, null);
        }

        FunctionGenerator generator;
        if (mainFunction != null) {
            generator = new FunctionGenerator(context, mainFunction);
        }
        else {
            TCTree.TCFunctionTree mainFunc = new ScriptMainFunc(node.statements);
            generator = new FunctionGenerator(context, mainFunc);
        }
        generator.stackGrows();
        generator.stackShrinks();

        file.entryPoint = generator.addInstructions(preloadInstructions).genBody().genReturn(null).complete();

        return null;
    }

    @Override
    public Void visitFunction(TCTree.TCFunctionTree node, Void unused) {
        if (node.getName().equals("__main__")){
            // handle main function at the end when all
            // global loadings are detected and generated
            mainFunction = node;
            return null;
        }

        if (node.getModifiers().getFlags().contains(Modifier.NATIVE)){
            int poolAddr = PoolPutter.putUtf8(context, node.getName());
            preloadInstructions.add(new LoadNative(poolAddr));
        }
        else {
            FunctionGenerator generator = new FunctionGenerator(context, node);
            int index = generator.genParams().genBody().genReturn(null).complete();
            preloadInstructions.add(new LoadVirtual(index));
        }
        preloadInstructions.add(new StoreGlobal(node.sym.address));
        return null;
    }

    @Override
    public Void visitClass(TCTree.TCClassTree node, Void unused) {
        ClassGenerator generator = new ClassGenerator(context, node);
        int index = generator.generate();
        preloadInstructions.add(new LoadType(index));
        preloadInstructions.add(new StoreGlobal(node.sym.address));
        return null;
    }

    @Override
    public Void visitNamespace(TCTree.TCNamespaceTree node, Void unused) {
        return visitClass(new NamespaceClass(node), null);
    }
}
