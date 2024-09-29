package com.tscript.compiler.impl.analyze;

import com.tscript.compiler.impl.utils.*;
import com.tscript.compiler.impl.utils.TCTree.*;
import com.tscript.compiler.source.utils.ClassNameFormatter;

import java.util.*;

public class HierarchyResolver {

    public static void resolve(TCTree tree, ClassNameFormatter fmt){
        Collector collector = new Collector();
        tree.accept(collector);

        final Hierarchy globalHierarchy = collector.globals;
        final Queue<CheckTask> laterTasks = collector.laterTasks;

        // apply inheritance to the symbols
        // and check for invalid inheritance, e.g.
        // super class not found or super is not a class
        for (CheckTask task : laterTasks) {
            // is the super class in the current defined local scope?
            Symbol sym = task.owner.resolve(task.superName.iterator());
            if (sym != null){
                if (sym.kind == Symbol.Kind.CLASS){

                    task.tree.sym.superClass = (Symbol.ClassSymbol) sym;
                }
                else {
                    throwSuperClassNotFound(task, fmt);
                }
            }
            else {
                // is the super class in the global scope?
                sym = globalHierarchy.resolve(task.superName.iterator());
                if (sym != null){
                    if (sym.kind == Symbol.Kind.CLASS){
                        task.tree.sym.superClass = (Symbol.ClassSymbol) sym;
                    }
                    else {
                        throwSuperClassNotFound(task, fmt);
                    }
                }
                else {
                    throwSuperClassNotFound(task, fmt);
                }
            }
        }

        // check for infinite inheritance cycle
        Set<Symbol.ClassSymbol> done = new HashSet<>();
        for (Symbol.ClassSymbol clazz : collector.allClasses){
            if (done.contains(clazz))
                continue;
            traverseInheritancePath(clazz, done, new HashSet<>());
        }
    }

    private static void traverseInheritancePath(
            Symbol.ClassSymbol curr,
            Set<Symbol.ClassSymbol> done,
            Set<Symbol.ClassSymbol> visited) {

        if (visited.contains(curr))
            throw Errors.hasInfiniteInheritance(curr.location);

        done.add(curr);
        visited.add(curr);

        Symbol.ClassSymbol superClass = curr.superClass;
        if (superClass == null) return;
        traverseInheritancePath(superClass, done, visited);
    }

    private static void throwSuperClassNotFound(CheckTask task, ClassNameFormatter fmt){
        throw Errors.canNotFindClass(fmt.format(task.superName), task.tree.location);
    }
    
    private static class Hierarchy {
        private final Map<String, Hierarchy> children = new HashMap<>();
        private final Symbol symbol;
        public Hierarchy(Symbol symbol) {
            this.symbol = symbol;
        }
        public Hierarchy(){
            this(null);
        }

        public Symbol resolve(Iterator<String> itr) {
            if (!itr.hasNext()) return symbol;
            String className = itr.next();
            Hierarchy next = children.get(className);
            if (next == null) return null;
            return next.resolve(itr);
        }

    }

    
    private record CheckTask(TCClassTree tree, List<String> superName, Hierarchy owner) {
    }


    private static class Collector extends TCTreeScanner<Hierarchy, Void> {
        
        Hierarchy globals = new Hierarchy();
        Queue<CheckTask> laterTasks = new LinkedList<>();
        List<Symbol.ClassSymbol> allClasses = new ArrayList<>();

        @Override
        public Void visitRoot(TCRootTree node, Hierarchy unused) {
            return super.visitRoot(node, globals);
        }

        @Override
        public Void visitClass(TCClassTree node, Hierarchy hierarchy) {
            Hierarchy classHierarchy = new Hierarchy(node.sym);
            hierarchy.children.put(node.getName(), classHierarchy);
            allClasses.add(node.sym);

            if (hasSuperClass(node))
                laterTasks.add(new CheckTask(node, node.superName, hierarchy));
            
            return super.visitClass(node, classHierarchy);
        }

        @Override
        public Void visitNamespace(TCNamespaceTree node, Hierarchy hierarchy) {
            Hierarchy namespaceHierarchy = new Hierarchy(node.sym);
            hierarchy.children.put(node.getName(), namespaceHierarchy);
            return super.visitNamespace(node, namespaceHierarchy);
        }

        @Override
        public Void visitFunction(TCFunctionTree node, Hierarchy hierarchy) {
            hierarchy = new Hierarchy(node.sym);
            return super.visitFunction(node, hierarchy);
        }

        @Override
        public Void visitLambda(TCLambdaTree node, Hierarchy hierarchy) {
            hierarchy = new Hierarchy();
            return super.visitLambda(node, hierarchy);
        }

        private static boolean hasSuperClass(TCClassTree node) {
            return node.superName != null && !node.superName.isEmpty();
        }
    }

}
