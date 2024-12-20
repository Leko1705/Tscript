package com.tscript.compiler.impl.analyze;

import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.impl.utils.Errors;
import com.tscript.compiler.source.utils.TreeScanner;

import java.io.BufferedReader;
import java.util.Set;

public class ScopeChecker {


    public static void check(Tree tree){
        tree.accept(new Checker());
    }


    private static class Checker extends TreeScanner<Void, Void> {

        private boolean inFunction = false;
        private boolean inStaticScope = false;
        private boolean inConstructor = false;
        private boolean inClass = false;
        private boolean inLoop = false;
        private boolean inBreakableCase = false;
        private boolean inLambda = false;
        private boolean inAbstractScope = false;
        private boolean inNamespace = false;

        @Override
        public Void visitClass(ClassTree node, Void unused) {
            boolean inClass = this.inClass;
            boolean inAbstractScope = this.inAbstractScope;
            boolean inNamespace = this.inNamespace;
            this.inClass = true;
            this.inNamespace = false;
            this.inAbstractScope = node.getModifiers().getFlags().contains(Modifier.ABSTRACT);
            super.visitClass(node, unused);
            this.inClass = inClass;
            this.inAbstractScope = inAbstractScope;
            this.inNamespace = inNamespace;
            return null;
        }

        @Override
        public Void visitNamespace(NamespaceTree node, Void unused) {
            boolean inNamespace = this.inNamespace;
            this.inNamespace = true;
            super.visitNamespace(node, unused);
            this.inNamespace = inNamespace;
            return null;
        }

        @Override
        public Void visitFunction(FunctionTree functionTree, Void unused) {

            Set<Modifier> modifiers = functionTree.getModifiers().getFlags();

            if (modifiers.contains(Modifier.ABSTRACT) && !inAbstractScope) {
                throw Errors.canNotDefineOutOfAbstractClass(functionTree.getLocation());
            }

            boolean inStaticScope = this.inStaticScope;
            this.inStaticScope = modifiers.contains(Modifier.STATIC);

            boolean inClass = this.inClass;
            boolean inNamespace = this.inNamespace;
            if (inFunction) {
                // anonymous functions do not refer to 'this'
                this.inClass = false;
                this.inNamespace = false;
            }

            boolean inFunction = this.inFunction;
            this.inFunction = true;
            super.visitFunction(functionTree, unused);
            this.inFunction = inFunction;
            this.inClass = inClass;
            this.inNamespace = inNamespace;
            this.inStaticScope = inStaticScope;
            return null;
        }

        @Override
        public Void visitConstructor(ConstructorTree constructorTree, Void unused) {
            boolean inConstructor = this.inConstructor;
            this.inConstructor = true;
            super.visitConstructor(constructorTree, unused);
            this.inConstructor = inConstructor;
            return null;
        }

        @Override
        public Void visitWhileDoLoop(WhileDoTree whileDoTree, Void unused) {
            boolean inLoop = this.inLoop;
            this.inLoop = true;
            super.visitWhileDoLoop(whileDoTree, unused);
            this.inLoop = inLoop;
            return null;
        }

        @Override
        public Void visitDoWhileLoop(DoWhileTree doWhileTree, Void unused) {
            boolean inLoop = this.inLoop;
            this.inLoop = true;
            super.visitDoWhileLoop(doWhileTree, unused);
            this.inLoop = inLoop;
            return null;
        }

        @Override
        public Void visitForLoop(ForLoopTree forLoopTree, Void unused) {
            boolean inLoop = this.inLoop;
            this.inLoop = true;
            super.visitForLoop(forLoopTree, unused);
            this.inLoop = inLoop;
            return null;
        }

        @Override
        public Void visitLambda(LambdaTree lambdaTree, Void unused) {
            boolean inFunction = this.inFunction;
            boolean inClass = this.inClass;
            boolean inLoop = this.inClass;
            boolean inLambda = this.inLambda;
            boolean inConstructor = this.inConstructor;
            boolean inAbstractScope = this.inAbstractScope;
            boolean inNamespace = this.inNamespace;
            this.inFunction = true;
            this.inClass = false;
            this.inLoop = false;
            this.inLambda = true;
            this.inConstructor = false;
            this.inAbstractScope = false;
            super.visitLambda(lambdaTree, unused);
            this.inFunction = inFunction;
            this.inClass = inClass;
            this.inLoop = inLoop;
            this.inLambda = inLambda;
            this.inConstructor = inConstructor;
            this.inAbstractScope = inAbstractScope;
            this.inNamespace = inNamespace;
            return null;
        }

        @Override
        public Void visitBreak(BreakTree breakTree, Void unused) {
            if (!inLoop && !inBreakableCase)
                throw Errors.canNotBreakOutOfLoop(breakTree.getLocation());
            return null;
        }

        @Override
        public Void visitCase(CaseTree node, Void unused) {
            boolean inCase = this.inBreakableCase;
            inBreakableCase = node.allowBreak();
            scan(node.getStatement(), unused);
            this.inBreakableCase = inCase;
            return null;
        }

        @Override
        public Void visitContinue(ContinueTree continueTree, Void unused) {
            if (!inLoop)
                throw Errors.canNotContinueOutOfLoop(continueTree.getLocation());
            return null;
        }

        @Override
        public Void visitReturn(ReturnTree returnTree, Void unused) {
            if (!inFunction && !inConstructor)
                throw Errors.canNotReturnOutOfFunction(returnTree.getLocation());
            else if (inConstructor)
                throw Errors.canNotReturnFromConstructor(returnTree.getLocation());
            return null;
        }

        @Override
        public Void visitThis(ThisTree thisTree, Void unused) {
            if ((!inClass && !inLambda && !inFunction && !inConstructor) || inNamespace)
                throw Errors.canNotUseThisOutOfClassOrFunction(thisTree.getLocation());

            else if (inFunction && inStaticScope)
                throw Errors.canNotUseThisFromStaticContext(thisTree.getLocation());

            return null;
        }

        @Override
        public Void visitSuper(SuperTree superTree, Void unused) {
            if (!inClass || inNamespace)
                throw Errors.canNotUseSuperOutOfClass(superTree.getLocation());
            else if (inFunction && inStaticScope)
                throw Errors.canNotUseSuperFromStaticContext(superTree.getLocation());
            return null;
        }

    }

}
