package com.tscript.compiler.impl.generation.generators;

import com.tscript.compiler.impl.analyze.scoping.Scope;
import com.tscript.compiler.impl.analyze.structures.Symbol;
import com.tscript.compiler.impl.generation.compiled.GlobalVariable;
import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.source.utils.SimpleTreeVisitor;

import java.util.List;

public class GlobalRegistry extends SimpleTreeVisitor<List<GlobalVariable>, Void> {

    private final Scope scope;

    public GlobalRegistry(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Void visitFunction(FunctionTree node, List<GlobalVariable> globalVariables) {
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
        boolean mutable = scope.getSymbol(node.getName()).kind == Symbol.Kind.VARIABLE;
        globalVariables.add(new GlobalVariable(node.getName(), mutable));
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, List<GlobalVariable> globalVariables) {
        if (node.getVariable() != null){
            globalVariables.add(new GlobalVariable(node.getVariable().getName(), true));
        }
        return null;
    }

    @Override
    public Void visitBlock(BlockTree node, List<GlobalVariable> globalVariables) {
        for (StatementTree statement : node.getStatements())
            statement.accept(this, globalVariables);
        return null;
    }
}
