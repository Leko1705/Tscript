package com.tscript.compiler.impl.analyze;

import com.tscript.compiler.impl.utils.typing.BuiltInTypes;
import com.tscript.compiler.impl.utils.typing.Type;
import com.tscript.compiler.impl.utils.typing.TypeBuilder;
import com.tscript.compiler.impl.utils.typing.UnknownType;
import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.impl.utils.Errors;
import com.tscript.compiler.source.utils.TreeScanner;

import java.util.*;

public class TypeChecker {

    public static void check(Tree tree){
        TypeResolver resolver = new TypeResolver();
        tree.accept(resolver);
        Map<String, Type> types = resolver.types;
        tree.accept(new Checker(types));
    }

    private static class TypeResolver extends TreeScanner<List<String>, Void> {

        private boolean inFunction = false;

        private final Map<String, Type> types = new HashMap<>(BuiltInTypes.get());

        @Override
        public Void visitRoot(RootTree node, List<String> unused) {
            return super.visitRoot(node, new LinkedList<>());
        }

        @Override
        public Void visitClass(ClassTree node, List<String> chain) {
            chain.add(node.getName());

            StringBuilder fullName = new StringBuilder();
            Iterator<String> iterator = chain.iterator();
            if (iterator.hasNext()) {
                fullName.append(iterator.next());
                while (iterator.hasNext()) {
                    fullName.append(".").append(iterator.next());
                }
            }

            Type newType = TypeBuilder.newBuilder(fullName.toString())
                    .setCallable(false)
                    .setItemAccessible(false)
                    .create();

            types.put(fullName.toString(), newType);

            boolean inFunction = this.inFunction;
            this.inFunction = false; // prevent incorrect nested function-class-function construction handling
            super.visitClass(node, chain);
            this.inFunction = inFunction;

            chain.remove(chain.size() - 1);
            return null;
        }

        @Override
        public Void visitFunction(FunctionTree node, List<String> chain) {
            List<String> passed = inFunction ? new ArrayList<>() : chain;
            boolean inFunction = this.inFunction;
            this.inFunction = true;
            super.visitFunction(node, passed);
            this.inFunction = inFunction;
            return null;
        }

        @Override
        public Void visitNamespace(NamespaceTree node, List<String> chain) {
            chain.add(node.getName());
            super.visitNamespace(node, chain);
            chain.remove(chain.size() - 1);
            return null;
        }
    }

    private static class Checker extends TreeScanner<Void, Type> {

        private final Map<String, Type> types;

        private Checker(Map<String, Type> types) {
            this.types = types;
        }

        @Override
        public Type visitInteger(IntegerTree node, Void unused) {
            return types.get("Integer");
        }

        @Override
        public Type visitFloat(FloatTree node, Void unused) {
            return types.get("Real");
        }

        @Override
        public Type visitString(StringTree node, Void unused) {
            return types.get("String");
        }

        @Override
        public Type visitBoolean(BooleanTree node, Void unused) {
            return types.get("Boolean");
        }

        @Override
        public Type visitNull(NullTree node, Void unused) {
            return types.get("Null");
        }

        @Override
        public Type visitArray(ArrayTree node, Void unused) {
            super.visitArray(node, unused);
            return types.get("Array");
        }

        @Override
        public Type visitDictionary(DictionaryTree node, Void unused) {
            super.visitDictionary(node, unused);
            return types.get("Dictionary");
        }

        @Override
        public Type visitLambda(LambdaTree node, Void unused) {
            super.visitLambda(node, unused);
            return types.get("Function");
        }

        @Override
        public Type visitRange(RangeTree node, Void unused) {
            Type from = scan(node.getFrom(), null);
            checkRequiredType(node, from, List.of("Integer"));
            Type to = scan(node.getTo(), null);
            checkRequiredType(node, to, List.of("Integer"));
            return types.get("Range");
        }

        @Override
        public Type visitCall(CallTree node, Void unused) {
            Type calledType = scan(node.getCalled(), null);
            if (!calledType.isCallable())
                throw Errors.notCallable(calledType.getName(), node.getLocation());
            scan(node.getArguments(), null);
            return UnknownType.INSTANCE;
        }

        @Override
        public Type visitAssign(AssignTree node, Void unused) {
            return scan(node.getRightOperand(), null);
        }

        @Override
        public Type visitNot(NotTree node, Void unused) {
            Type type = scan(node.getOperand(), null);
            checkRequiredType(node, type, List.of("Integer", "Boolean"));
            return type;
        }

        @Override
        public Type visitSign(SignTree node, Void unused) {
            Type type = scan(node.getOperand(), null);
            checkRequiredType(node, type, List.of("Integer", "Real"));
            return type;
        }

        @Override
        public Type visitGetType(GetTypeTree node, Void unused) {
            super.visitGetType(node, unused);
            return types.get("Type");
        }

        @Override
        public Type visitContainerAccess(ContainerAccessTree node, Void unused) {
            Type container = scan(node.getContainer(), null);

            if (!container.isItemAccessible()){
                throw Errors.notAccessible(container.getName(), node.getLocation());
            }

            Type key = scan(node.getKey(), null);

            if (container.getName().equals("Array")){
                checkRequiredType(node, key, List.of("Integer", "Range"));
                if (key.getName().equals("Integer")) return UnknownType.INSTANCE;
                if (key.getName().equals("Range")) return container; // Array-Type
                return UnknownType.INSTANCE;
            }
            else if (container.getName().equals("Range")){
                checkRequiredType(node, key, List.of("Integer", "Range"));
                if (key.getName().equals("Integer")) return key; // Integer-Type
                if (key.getName().equals("Range")) return container; // Range-Type
                return UnknownType.INSTANCE;
            }
            else if (container.getName().equals("String")){
                checkRequiredType(node, key, List.of("Integer", "Range"));
                return types.get("String");
            }
            else if (container.getName().equals("Dictionary")){
                return UnknownType.INSTANCE;
            }
            else if (container == UnknownType.INSTANCE) {
                return UnknownType.INSTANCE;
            }
            throw new AssertionError(container.getName());
        }

        @Override
        public Type visitThis(ThisTree node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public Type visitSuper(SuperTree node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public Type visitVariable(VariableTree node, Void unused) {
            return UnknownType.INSTANCE;
        }

        @Override
        public Type visitBinaryOperation(BinaryOperationTree node, Void unused) {
            Type left = scan(node.getLeftOperand(), null);
            Type right = scan(node.getRightOperand(), null);

            if (node.getOperationType() == Operation.EQUALS
                    || node.getOperationType() == Operation.NOT_EQUALS)
                return types.get("Boolean");

            Type result = left.operate(node.getOperationType(), right, types);
            if (result == null){
                throw Errors.canNotOperate(left.getName(), right.getName(), node.getOperationType(), node.getLocation());
            }

            return result;
        }

        @Override
        public Type visitForLoop(ForLoopTree node, Void unused) {
            scan(node.getVariable(), null);
            Type iterableType = scan(node.getIterable(), null);
            if (!iterableType.isItemAccessible()){
                throw Errors.notIterable(iterableType.getName(), node.getLocation());
            }
            scan(node.getStatement(), null);
            return null;
        }

        @Override
        public Type visitIfElse(IfElseTree node, Void unused) {
            checkRequiredType(node, scan(node.getCondition(), null), List.of("Boolean"));
            scan(node.getThenStatement(), null);
            scan(node.getElseStatement(), null);
            return null;
        }

        @Override
        public Type visitWhileDoLoop(WhileDoTree node, Void unused) {
            checkRequiredType(node, scan(node.getCondition(), null), List.of("Boolean"));
            scan(node.getStatement(), null);
            return null;
        }

        @Override
        public Type visitDoWhileLoop(DoWhileTree node, Void unused) {
            scan(node.getStatement(), null);
            checkRequiredType(node, scan(node.getCondition(), null), List.of("Boolean"));
            return null;
        }

        @Override
        public Type visitMemberAccess(MemberAccessTree node, Void unused) {
            super.visitMemberAccess(node, unused);
            return UnknownType.INSTANCE;
        }

        private void checkRequiredType(Tree caller, Type got, List<String> required){
            if (got == UnknownType.INSTANCE) return;
            if (!required.contains(got.toString())){
                throw Errors.requiredButGotType(required, got.getName(), caller.getLocation());
            }
        }

    }

}
