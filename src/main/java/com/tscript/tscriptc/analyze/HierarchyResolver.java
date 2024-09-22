package com.tscript.tscriptc.analyze;

import com.tscript.tscriptc.analyze.scoping.ExternalScope;
import com.tscript.tscriptc.analyze.scoping.Scope;
import com.tscript.tscriptc.analyze.scoping.ScopeVisitor;
import com.tscript.tscriptc.analyze.structures.Symbol;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Errors;
import com.tscript.tscriptc.utils.TreeScanner;

import java.util.*;

public class HierarchyResolver {

    public static void resolve(Tree tree, Scope scope) {
        Resolver resolver = new Resolver();
        tree.accept(resolver, scope);
        TreeHierarchy hierarchy = new TreeHierarchy(resolver.root);
        tree.accept(new Checker(hierarchy), scope);
    }


    private record TreeHierarchy(HierarchyResolver.TreeHierarchy.Node root) {

        public static class Node {
            private final Symbol symbol;
            private final Map<String, Node> children;
            private final boolean canContinueResolve;

            public Node(Symbol symbol, Map<String, Node> children, boolean canContinueResolve) {
                this.symbol = symbol;
                this.children = children;
                this.canContinueResolve = canContinueResolve;
            }

            public Symbol resolve(Iterator<String> iterator) {
                if (!iterator.hasNext()) return symbol;
                if (!canContinueResolve) return null;
                String key = iterator.next();
                Node node = children.get(key);
                if (node == null) return null;
                return node.resolve(iterator);
            }
        }

        public Symbol resolveDefinition(List<String> accessChain) {
            return root.resolve(accessChain.iterator());
        }
    }



    private static class Resolver extends TreeScanner<Scope, Map<String, TreeHierarchy.Node>> {

        private TreeHierarchy.Node root;

        @Override
        public Map<String, TreeHierarchy.Node> selectResult(Map<String, TreeHierarchy.Node> r1,
                                                            Map<String, TreeHierarchy.Node> r2) {
            if (r1 == null && r2 == null) return null;
            if (r1 == null) return r2;
            if (r2 == null) return r1;
            Map<String, TreeHierarchy.Node> merged = new HashMap<>(r1);
            merged.putAll(r2);
            return merged;
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitRoot(RootTree node, Scope rootScope) {
            Map<String, TreeHierarchy.Node> children = super.visitRoot(node, rootScope);
            root = new TreeHierarchy.Node(null, children, true);
            return null;
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitClass(ClassTree node, Scope scope) {
            Map<String, TreeHierarchy.Node> children = super.visitClass(node, notNull(scope.getChildScope(node)));
            return Map.of(node.getName(), new TreeHierarchy.Node(notNull(scope.getSymbol(node.getName())), children, true));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitNamespace(NamespaceTree node, Scope scope) {
            Map<String, TreeHierarchy.Node> children = super.visitNamespace(node, notNull(scope.getChildScope(node)));
            return Map.of(node.getName(), new TreeHierarchy.Node(null, children, true));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitFunction(FunctionTree node, Scope scope) {
            Map<String, TreeHierarchy.Node> children = super.visitFunction(node, notNull(scope.getChildScope(node)));
            return Map.of(node.getName(), new TreeHierarchy.Node(null, children, false));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitBlock(BlockTree node, Scope scope) {
            return super.visitBlock(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitForLoop(ForLoopTree node, Scope scope) {
            return super.visitForLoop(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitTryCatch(TryCatchTree node, Scope scope) {
            scan(node.getTryStatement(), scope);
            scope = notNull(scope.getChildScope(node));
            scan(node.getExceptionVariable(), scope);
            scan(node.getCatchStatement(), scope);
            return null;
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitLambda(LambdaTree node, Scope scope) {
            return super.visitLambda(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitConstructor(ConstructorTree node, Scope scope) {
            return super.visitConstructor(node, notNull(scope.getChildScope(node)));
        }

    }



    private static class Checker extends TreeScanner<Scope, Void> {

        private final TreeHierarchy hierarchy;
        private final Map<Tree, Tree> inheritanceMap = new HashMap<>();

        private Checker(TreeHierarchy hierarchy) {
            this.hierarchy = hierarchy;
        }

        private void checkFiniteInheritanceCycle(){
            Set<Tree> done = new HashSet<>();
            for (Tree toCheck : inheritanceMap.keySet()){
                if (done.contains(toCheck))
                    continue;
                checkPath(toCheck, done);
            }
        }

        private void checkPath(Tree checked, Set<Tree> done){
            Set<Tree> inheritancePath = new HashSet<>();
            do {
                if (inheritancePath.contains(checked))
                    throw Errors.hasInfiniteInheritance(checked.getLocation());
                inheritancePath.add(checked);
                done.add(checked);
                checked = inheritanceMap.get(checked);
            }
            while (checked != null);
        }

        @Override
        public Void visitRoot(RootTree node, Scope scope) {
            super.visitRoot(node, scope);
            checkFiniteInheritanceCycle();
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Scope scope) {
            if (node.getSuperName() != null){
                List<String> accessChain = Arrays.asList(node.getSuperName().split("[.]"));
                Symbol superSymbol = hierarchy.resolveDefinition(accessChain);
                if (superSymbol == null && accessChain.size() == 1){
                    superSymbol = scope.getGlobalScope().getSymbol(accessChain.get(0));
                }
                if (superSymbol == null || (superSymbol.kind != Symbol.Kind.CLASS && superSymbol.kind != Symbol.Kind.UNKNOWN)){
                    throw Errors.canNotFindClass(node.getSuperName(), node.getLocation());
                }
                else {
                    inheritanceMap.put(node, superSymbol.tree);
                }

                DefinitionResolver.ClassScopeBase base = (DefinitionResolver.ClassScopeBase) notNull(scope.getChildScope(node));

                if (superSymbol.kind == Symbol.Kind.CLASS){
                    base.superScope = superSymbol.scope.getChildScope(superSymbol.tree);
                }
                else {
                    Symbol finalSuperSymbol = superSymbol;
                    base.superScope = new ExternalScope() {
                        @Override
                        public String getName() {
                            return finalSuperSymbol.name;
                        }

                        @Override
                        public <P, R> R accept(ScopeVisitor<P, R> visitor, P param) {
                            return visitor.visitExternal(this, param);
                        }
                    };
                }
            }
            return super.visitClass(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Void visitNamespace(NamespaceTree node, Scope scope) {
            return super.visitNamespace(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Void visitFunction(FunctionTree node, Scope scope) {
            return super.visitFunction(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Void visitBlock(BlockTree node, Scope scope) {
            return super.visitBlock(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Scope scope) {
            return super.visitForLoop(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Void visitTryCatch(TryCatchTree node, Scope scope) {
            scan(node.getTryStatement(), scope);
            scope = scope.getChildScope(node);
            scan(node.getExceptionVariable(), scope);
            scan(node.getCatchStatement(), scope);
            return null;
        }

        @Override
        public Void visitLambda(LambdaTree node, Scope scope) {
            return super.visitLambda(node, notNull(scope.getChildScope(node)));
        }

        @Override
        public Void visitConstructor(ConstructorTree node, Scope scope) {
            return super.visitConstructor(node, notNull(scope.getChildScope(node)));
        }
    }

    private static <T> T notNull(T t) {
        return Objects.requireNonNull(t);
    }
}
