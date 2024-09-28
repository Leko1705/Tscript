package com.tscript.compiler.impl.analyze;

import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.impl.utils.Errors;
import com.tscript.compiler.source.utils.TreeScanner;

import java.util.Set;

public class PostSyntaxChecker {

    public static void check(Tree tree){
        tree.accept(new Checker());
    }


    private static class Checker extends TreeScanner<Void, Void> {

        boolean isConstant = false;

        @Override
        public Void visitFunction(FunctionTree node, Void unused) {
            scan(node.getParameters(), null);

            Set<Modifier> modifiers = node.getModifiers().getModifiers();

            if (modifiers.contains(Modifier.ABSTRACT) && modifiers.contains(Modifier.STATIC)) {
                throw Errors.canNotUseStaticOnAbstract(node.getLocation());
            }

            if (modifiers.contains(Modifier.ABSTRACT) && modifiers.contains(Modifier.PRIVATE)) {
                throw Errors.canNotUsePrivateOnAbstract(node.getLocation());
            }

            if (node.getBody() != null)
                scan(node.getBody(), null);

            return null;
        }


        @Override
        public Void visitVarDefs(VarDefsTree node, Void unused) {
            boolean isConstant = this.isConstant;
            this.isConstant = node.getModifiers().getModifiers().contains(Modifier.CONSTANT);
            scan(node.getDefinitions(), null);
            this.isConstant = isConstant;
            return null;
        }

        @Override
        public Void visitVarDef(VarDefTree node, Void unused) {
            if (node.getInitializer() == null && isConstant){
                throw Errors.constantMustBeInitialized(node.getLocation());
            }
            return super.visitVarDef(node, unused);
        }

        @Override
        public Void visitAssign(AssignTree node, Void unused) {
            if (node.getLeftOperand() instanceof CallTree){
                throw Errors.canNotAssignToCallResult(node.getLocation());
            }
            return super.visitAssign(node, unused);
        }
    }

}
