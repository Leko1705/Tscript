package com.tscript.tscriptc.analyze;

import com.tscript.tscriptc.analyze.structures.Hierarchy;
import com.tscript.tscriptc.analyze.structures.Symbol;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Errors;
import com.tscript.tscriptc.utils.TreeScanner;

import java.util.*;

public class HierarchyResolver {

    public static Hierarchy resolve(Tree tree) {
        Resolver resolver = new Resolver();
        tree.accept(resolver);
        Hierarchy hierarchy = new TreeHierarchy(resolver.root);
        tree.accept(new Checker(hierarchy));
        return hierarchy;
    }


    private record TreeHierarchy(HierarchyResolver.TreeHierarchy.Node root) implements Hierarchy {

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

        @Override
        public Symbol resolveDefinition(List<String> accessChain) {
            return root.resolve(accessChain.iterator());
        }
    }



    private static class Resolver extends TreeScanner<Void, Map<String, TreeHierarchy.Node>> {

        private TreeHierarchy.Node root;

        @Override
        public Map<String, TreeHierarchy.Node> selectResult(Map<String, TreeHierarchy.Node> r1,
                                                            Map<String, TreeHierarchy.Node> r2) {
            Map<String, TreeHierarchy.Node> merged = new HashMap<>(r1);
            merged.putAll(r2);
            return merged;
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitRoot(RootTree node, Void unused) {
            Map<String, TreeHierarchy.Node> children = super.visitRoot(node, null);
            root = new TreeHierarchy.Node(null, children, true);
            return null;
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitClass(ClassTree node, Void unused) {
            Map<String, TreeHierarchy.Node> children = super.visitClass(node, null);
            return Map.of(node.getName(), new TreeHierarchy.Node(null, children, true));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitNamespace(NamespaceTree node, Void unused) {
            Map<String, TreeHierarchy.Node> children = super.visitNamespace(node, null);
            return Map.of(node.getName(), new TreeHierarchy.Node(null, children, true));
        }

        @Override
        public Map<String, TreeHierarchy.Node> visitFunction(FunctionTree node, Void unused) {
            Map<String, TreeHierarchy.Node> children = super.visitFunction(node, null);
            return Map.of(node.getName(), new TreeHierarchy.Node(null, children, false));
        }
    }



    private static class Checker extends TreeScanner<Void, Void> {

        private final Hierarchy hierarchy;
        private final Map<Symbol, Symbol> inheritanceMap = new HashMap<>();

        private Checker(Hierarchy hierarchy) {
            this.hierarchy = hierarchy;
        }

        private void checkFiniteInheritanceCycle(){
            Set<Symbol> done = new HashSet<>();
            for (Symbol toCheck : inheritanceMap.keySet()){
                if (done.contains(toCheck))
                    continue;
                checkPath(toCheck, done);
            }
        }

        private void checkPath(Symbol checked, Set<Symbol> done){
            Set<Symbol> inheritancePath = new HashSet<>();
            do {
                if (inheritancePath.contains(checked))
                    throw Errors.hasInfiniteInheritance(checked.tree.getLocation());
                inheritancePath.add(checked);
                done.add(checked);
                checked = inheritanceMap.get(checked);
            }
            while (checked != null);
        }

        @Override
        public Void visitRoot(RootTree node, Void unused) {
            super.visitRoot(node, unused);
            checkFiniteInheritanceCycle();
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Void unused) {
            if (node.getSuperName() != null){
                List<String> accessChain = Arrays.asList(node.getSuperName().split("[.]"));
                Symbol superSymbol = hierarchy.resolveDefinition(accessChain);
                if (superSymbol == null || superSymbol.kind != Symbol.Kind.CLASS){
                    throw Errors.canNotFindClass(node.getSuperName(), node.getLocation());
                }
                else {
                    inheritanceMap.put(null, superSymbol);
                }
            }
            return super.visitClass(node, unused);
        }
    }

}
