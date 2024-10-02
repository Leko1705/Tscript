package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.GlobalVariable;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.source.utils.SimpleTreeVisitor;

import java.util.List;

public class GlobalRegistry extends SimpleTreeVisitor<List<GlobalVariable>, Void> {

    @Override
    public Void visitFunction(FunctionTree node, List<GlobalVariable> globalVariables) {
        globalVariables.add(new GlobalVariable(node.getName(), false));
        return null;
    }

    @Override
    public Void visitClass(ClassTree node, List<GlobalVariable> globalVariables) {
        globalVariables.add(new GlobalVariable(node.getName(), false));
        return null;
    }

    @Override
    public Void visitNamespace(NamespaceTree node, List<GlobalVariable> globalVariables) {
        globalVariables.add(new GlobalVariable(node.getName(), false));
        return null;
    }

    @Override
    public Void visitVarDefs(VarDefsTree node, List<GlobalVariable> globalVariables) {
        for (VarDefTree def : node.getDefinitions())
            def.accept(this, globalVariables);
        return null;
    }

    @Override
    public Void visitVarDef(VarDefTree node, List<GlobalVariable> globalVariables) {
        TCTree.TCVarDefTree def = (TCTree.TCVarDefTree) node;
        boolean mutable = !def.sym.modifiers.contains(Modifier.CONSTANT);
        globalVariables.add(new GlobalVariable(node.getName(), mutable));
        return null;
    }

}
