package tscriptc.analysis;

import tscriptc.log.Logger;
import tscriptc.tree.*;
import tscriptc.util.Diagnostics;
import tscriptc.util.Errors;

import java.util.*;

public class TypeChecker extends Checker<Void, String> {

    private final ArrayDeque<String> classStack = new ArrayDeque<>();

    @Override
    public String visitRootTree(RootTree rootTree, Void unused) {
        super.visitRootTree(rootTree, unused);
        return null;
    }

    @Override
    public String visitClassTree(ClassTree classTree, Void unused) {
        classStack.push(classTree.getName());
        super.visitClassTree(classTree, unused);
        classStack.pop();
        return null;
    }

    @Override
    public String visitIntegerTree(IntegerLiteralTree tree, Void unused) {
        return "Integer";
    }

    @Override
    public String visitFloatTree(FloatLiteralTree tree, Void unused) {
        return "Real";
    }

    @Override
    public String visitBooleanTree(BooleanLiteralTree tree, Void unused) {
        return "Boolean";
    }

    @Override
    public String visitStringTree(StringLiteralTree tree, Void unused) {
        return "String";
    }

    @Override
    public String visitNullTree(NullLiteralTree tree, Void unused) {
        return "NullType";
    }

    @Override
    public String visitArrayTree(ArrayTree arrayTree, Void unused) {
        scan(arrayTree.getContent(), null);
        return "Array";
    }

    @Override
    public String visitDictionaryTree(DictionaryTree dictionaryTree, Void unused) {
        scan(dictionaryTree.getKeys(), null);
        scan(dictionaryTree.getValues(), null);
        return "Dictionary";
    }

    @Override
    public String visitLambdaTree(LambdaTree lambdaTree, Void unused) {
        super.visitLambdaTree(lambdaTree, unused);
        return "Function";
    }

    @Override
    public String visitThisTree(ThisTree thisTree, Void unused) {
        return classStack.peek();
    }

    @Override
    public String visitNotTree(NotTree notTree, Void unused) {
        String type = scan(notTree.getExpression(), null);
        if (!Types.canOperateNot(type))
            report(Errors.requiredButGotType(List.of("Integer", "Boolean"), type, notTree.getLocation()));
        return type;
    }

    @Override
    public String visitSignTree(SignTree signTree, Void unused) {
        String type = scan(signTree.getExpression(), null);
        if (!Types.canSign(type))
            report(Errors.requiredButGotType(List.of("Integer", "Real"), type, signTree.getLocation()));
        return type;
    }

    @Override
    public String visitOperationTree(BinaryOperationTree operationTree, Void unused) {
        Operation operation = operationTree.getOperation();
        String left = scan(operationTree.getLeft(), null);
        String right = scan(operationTree.getRight(), null);

        if (left != null && right != null) {
            if (!Types.canOperate(left, right, operation)) {
                report(Errors.canNotOperate(left, right, operation, operationTree.getLocation()));
            }
            else {
                return Types.getTypeFromOperation(left, right, operation);
            }
        }

        return null;
    }

    @Override
    public String visitRangeTree(RangeTree rangeTree, Void unused) {
        String left = scan(rangeTree.getFrom(), null);
        String right = scan(rangeTree.getTo(), null);

        if (left != null && !left.equals("Integer"))
            report(Errors.requiredButGotType(List.of("Integer"), left, rangeTree.getFrom().getLocation()));

        if (right != null && !right.equals("Integer"))
            report(Errors.requiredButGotType(List.of("Integer"), right, rangeTree.getTo().getLocation()));

        return "Range";
    }

    @Override
    public String visitCallTree(CallTree callTree, Void unused) {
        String calledType = scan(callTree.getExpression(), null);
        scan(callTree.getArguments(), null);

        if (!Types.isCallable(calledType))
            report(Errors.notCallable(calledType, callTree.getLocation()));

        return null;
    }

    @Override
    public String visitContainerAccessTree(ContainerAccessTree accessTree, Void unused) {

        String type = scan(accessTree.getExpression(), null);
        if (!Types.isContainerAccessible(type))
            report(Errors.notAccessible(type, accessTree.getLocation()));

        // since any expression is allowed to be a key
        // we don't perform a type check
        scan(accessTree.getKey(), null);

        return null;
    }

    @Override
    public String visitGetTypeTree(GetTypeTree getTypeTree, Void unused) {
        scan(getTypeTree.getExpression(), null);
        return "Type";
    }

    @Override
    public String visitForLoopTree(ForLoopTree forLoopTree, Void unused) {

        String iterableType = scan(forLoopTree.getIterable(), null);
        if (!Types.isIterable(iterableType))
            report(Errors.notIterable(iterableType, forLoopTree.getIterable().getLocation()));

        scan(forLoopTree.getBody(), null);
        return null;
    }

    @Override
    public String visitMemberAccessTree(MemberAccessTree accessTree, Void unused) {
        return null;
    }
}
