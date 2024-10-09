package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.instruction.Import;
import com.tscript.compiler.impl.generation.compiled.instruction.LoadExternal;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.impl.utils.TCTreeScanner;

import java.util.Iterator;

public class ImportGenerator extends TCTreeScanner<FunctionGenerator, Void> {

    private final Context context;

    public ImportGenerator(Context context) {
        this.context = context;
    }

    @Override
    public Void visitImport(TCTree.TCImportTree node, FunctionGenerator generator) {
        generator.newLine(node);

        Iterator<String> itr = node.accessChain.iterator();
        String name = itr.next();

        generator.func.getInstructions().add(new Import(PoolPutter.putUtf8(context, name)));
        generator.stackGrows();

        while (itr.hasNext()) {
            name = itr.next();
            generator.func.getInstructions().add(new LoadExternal(PoolPutter.putUtf8(context, name)));
        }

        TCTree.TCVariableTree var = new TCTree.TCVariableTree(node.location, name);
        var.sym = node.sym;

        new AssignGenerator(generator, context, generator.func).visitVariable(var, null);

        return null;
    }

    @Override
    public Void visitFromImport(TCTree.TCFromImportTree node, FunctionGenerator generator) {
        generator.newLine(node);

        Iterator<String> itr = node.fromChain.iterator();
        String name = itr.next();

        generator.func.getInstructions().add(new Import(PoolPutter.putUtf8(context, name)));
        generator.stackGrows();

        while (itr.hasNext()) {
            name = itr.next();
            generator.func.getInstructions().add(new LoadExternal(PoolPutter.putUtf8(context, name)));
        }

        itr = node.importChain.iterator();
        while (itr.hasNext()) {
            name = itr.next();
            generator.func.getInstructions().add(new LoadExternal(PoolPutter.putUtf8(context, name)));
        }

        TCTree.TCVariableTree var = new TCTree.TCVariableTree(node.location, name);
        var.sym = node.sym;

        new AssignGenerator(generator, context, generator.func).visitVariable(var, null);

        return null;
    }
}
