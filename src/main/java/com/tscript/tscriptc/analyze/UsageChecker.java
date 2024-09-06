package com.tscript.tscriptc.analyze;

import com.tscript.tscriptc.analyze.structures.*;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Errors;
import com.tscript.tscriptc.utils.SimpleTreeVisitor;
import com.tscript.tscriptc.utils.TreeScanner;

public class UsageChecker {

    public void check(Tree tree, Scope rootScope, Hierarchy hierarchy) {
        tree.accept(new Checker(rootScope, hierarchy));
    }


    private static class Checker extends TreeScanner<Scope, Void> {

        private final Scope rootScope;
        private final Hierarchy hierarchy;

        private Checker(Scope rootScope, Hierarchy hierarchy) {
            this.rootScope = rootScope;
            this.hierarchy = hierarchy;
        }

        @Override
        public Void visitRoot(RootTree node, Scope unused) {
            return super.visitRoot(node, rootScope);
        }

        @Override
        public Void visitBlock(BlockTree node, Scope scope) {
            scope = enterScope(scope, node);
            super.visitBlock(node, scope);
            return null;
        }

        @Override
        public Void visitConstructor(ConstructorTree node, Scope scope) {
            scope = enterScope(scope, node);
            super.visitConstructor(node, scope);
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Scope scope) {
            scope = enterScope(scope, node);
            super.visitClass(node, scope);
            return null;
        }

        @Override
        public Void visitFunction(FunctionTree node, Scope scope) {
            scope = enterScope(scope, node);
            super.visitFunction(node, scope);
            return null;
        }

        @Override
        public Void visitNamespace(NamespaceTree node, Scope scope) {
            scope = enterScope(scope, node);
            super.visitNamespace(node, scope);
            return null;
        }

        @Override
        public Void visitLambda(LambdaTree node, Scope scope) {
            scope = enterScope(scope, node);
            super.visitLambda(node, scope);
            return null;
        }

        @Override
        public Void visitVariable(VariableTree node, Scope scope) {

            while (scope != null){
                Symbol sym = scope.content.get(node.getName());
                if (sym != null) return null;
                if (scope.kind == Scope.Kind.CLASS){
                    ClassTree cls = (ClassTree) scope.owner;
                    if (checkSuperClasses(node.getName(), cls))
                        // found valid in inheritance hierarchy
                        return null;
                }
            }

            throw Errors.canNotFindSymbol(node.getName(), node.getLocation());
        }

        private static Scope enterScope(Scope scope, Object key){
            return scope.children.get(key);
        }

        private boolean checkSuperClasses(String name, ClassTree cls){
            Symbol[] found = new Symbol[]{null};

            for (ClassMemberTree member : cls.getMembers()){
                member.accept(new SimpleTreeVisitor<Boolean, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Boolean aBoolean) {

                        return null;
                    }
                });
                if (found[0] == null)
                    return false;
            }

            return false;
        }

    }

}
