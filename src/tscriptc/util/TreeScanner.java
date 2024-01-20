package tscriptc.util;

import tscriptc.tree.*;

import java.util.List;

public class TreeScanner<P, R> implements TreeVisitor<P, R> {

    public R selectResult(R r1, R r2){
        return r1;
    }

    public R scan(Tree tree, P p){
        return (tree != null) ? tree.accept(this, p) : null;
    }

    public R scan(List<? extends Tree> trees, P p){
        R r = null;
        for (Tree tree : trees)
            r = scanSelective(tree, p, r);
        return r;
    }

    public R scanSelective(Tree tree, P p, R r){
        return selectResult(scan(tree, p), r);
    }

    public R scanSelective(List<? extends Tree> trees, P p, R r){
        for (Tree tree : trees)
            r = scanSelective(tree, p, r);
        return r;
    }

    @Override
    public R visitRootTree(RootTree rootTree, P p) {
        R r = scan(rootTree.getDefinitions(), p);
        r = scanSelective(rootTree.getStatements(), p, r);
        return r;
    }

    @Override
    public R visitNamespaceTree(NamespaceTree namespaceTree, P p) {
        return scan(namespaceTree.getDefinitions(), p);
    }

    @Override
    public R visitClassTree(ClassTree classTree, P p) {
        R r = scan(classTree.getConstructor(), p);
        r = scanSelective(classTree.getDefinitions(), p, r);
        return r;
    }

    @Override
    public R visitConstructorTree(ConstructorTree constructorTree, P p) {
        R r = scan(constructorTree.getParameters(), p);
        r = scanSelective(constructorTree.getSuperArguments(), p, r);
        r = scanSelective(constructorTree.getBody(), p, r);
        return r;
    }

    @Override
    public R visitFunctionTree(FunctionTree functionTree, P p) {
        R r = scan(functionTree.getParameters(), p);
        r = scanSelective(functionTree.getBody(), p, r);
        return r;
    }

    @Override
    public R visitParameterTree(ParameterTree parameterTree, P p) {
        return scan(parameterTree.getInitializer(), p);
    }

    @Override
    public R visitNativeFunctionTree(NativeFunctionTree nativeFunctionTree, P p) {
        return null;
    }

    @Override
    public R visitAbstractMethodTree(AbstractMethodTree abstractMethodTree, P p) {
        return null;
    }

    @Override
    public R visitImportTree(ImportTree importTree, P p) {
        return null;
    }

    @Override
    public R visitUseTree(UseTree useTree, P p) {
        return null;
    }

    @Override
    public R visitVarDecTree(VarDecTree varDecTree, P p) {
        return scan(varDecTree.getInitializer(), p);
    }

    @Override
    public R visitIfElseTree(IfElseTree ifElseTree, P p) {
        R r = scan(ifElseTree.getCondition(), p);
        r = scanSelective(ifElseTree.getIfBody(), p, r);
        r = scanSelective(ifElseTree.getElseBody(), p, r);
        return r;
    }

    @Override
    public R visitWhileDoTree(WhileDoTree whileDoTree, P p) {
        R r = scan(whileDoTree.getCondition(), p);
        r = scanSelective(whileDoTree.getBody(), p, r);
        return r;
    }

    @Override
    public R visitDoWhileTree(DoWhileTree doWhileTree, P p) {
        R r = scan(doWhileTree.getBody(), p);
        r = scanSelective(doWhileTree.getCondition(), p, r);
        return r;
    }

    @Override
    public R visitForLoopTree(ForLoopTree forLoopTree, P p) {
        R r = scan(forLoopTree.getIterable(), p);
        r = scanSelective(forLoopTree.getBody(), p, r);
        return r;
    }

    @Override
    public R visitBreakTree(BreakTree breakTree, P p) {
        return null;
    }

    @Override
    public R visitContinueTree(ContinueTree continueTree, P p) {
        return null;
    }

    @Override
    public R visitReturnTree(ReturnTree returnTree, P p) {
        return scan(returnTree.getExpression(), p);
    }

    @Override
    public R visitTryCatchTree(TryCatchTree tryCatchTree, P p) {
        R r = scan(tryCatchTree.getTryBody(), p);
        r = scanSelective(tryCatchTree.getCatchBody(), p, r);
        return r;
    }

    @Override
    public R visitThrowTree(ThrowTree throwTree, P p) {
        return scan(throwTree.getExpression(), p);
    }

    @Override
    public R visitBlockTree(BlockTree blockTree, P p) {
        return scan(blockTree.getStatements(), p);
    }

    @Override
    public R visitExpressionStatementTree(ExpressionStatementTree expressionStatementTree, P p) {
        return scan(expressionStatementTree.getExpression(), p);
    }

    @Override
    public R visitIdentifierTree(IdentifierTree identifierTree, P p) {
        return null;
    }

    @Override
    public R visitAssignTree(AssignTree assignTree, P p) {
        R r = scan(assignTree.getLeft(), p);
        r = scanSelective(assignTree.getRight(), p, r);
        return r;
    }

    @Override
    public R visitThisTree(ThisTree thisTree, P p) {
        return null;
    }

    @Override
    public R visitSuperTree(SuperTree superTree, P p) {
        return null;
    }

    @Override
    public R visitNullTree(NullLiteralTree tree, P p) {
        return null;
    }

    @Override
    public R visitIntegerTree(IntegerLiteralTree tree, P p) {
        return null;
    }

    @Override
    public R visitFloatTree(FloatLiteralTree tree, P p) {
        return null;
    }

    @Override
    public R visitBooleanTree(BooleanLiteralTree tree, P p) {
        return null;
    }

    @Override
    public R visitStringTree(StringLiteralTree tree, P p) {
        return null;
    }

    @Override
    public R visitCallTree(CallTree callTree, P p) {
        R r = scan(callTree.getExpression(), p);
        r = scanSelective(callTree.getArguments(), p, r);
        return r;
    }

    @Override
    public R visitArgumentTree(ArgumentTree argumentTree, P p) {
        return scan(argumentTree.getExpression(), p);
    }

    @Override
    public R visitRangeTree(RangeTree rangeTree, P p) {
        R r = scan(rangeTree.getFrom(), p);
        r = scanSelective(rangeTree.getTo(), p, r);
        return r;
    }

    @Override
    public R visitArrayTree(ArrayTree arrayTree, P p) {
        return scan(arrayTree.getContent(), p);
    }

    @Override
    public R visitDictionaryTree(DictionaryTree dictionaryTree, P p) {
        R r = scan(dictionaryTree.getKeys(), p);
        r = scanSelective(dictionaryTree.getValues(), p, r);
        return r;
    }

    @Override
    public R visitContainerAccessTree(ContainerAccessTree accessTree, P p) {
        R r = scan(accessTree.getExpression(), p);
        r = scanSelective(accessTree.getKey(), p, r);
        return r;
    }

    @Override
    public R visitMemberAccessTree(MemberAccessTree accessTree, P p) {
        return scan(accessTree.getExpression(), p);
    }

    @Override
    public R visitLambdaTree(LambdaTree lambdaTree, P p) {
        R r = scan(lambdaTree.getClosures(), p);
        r = scanSelective(lambdaTree.getParameters(), p, r);
        r = scanSelective(lambdaTree.getBody(), p, r);
        return r;
    }

    @Override
    public R visitClosureTree(ClosureTree closureTree, P p) {
        return scan(closureTree.getExpression(), p);
    }

    @Override
    public R visitNotTree(NotTree notTree, P p) {
        return scan(notTree.getExpression(), p);
    }

    @Override
    public R visitSignTree(SignTree signTree, P p) {
        return scan(signTree.getExpression(), p);
    }

    @Override
    public R visitOperationTree(BinaryOperationTree operationTree, P p) {
        R r = scan(operationTree.getLeft(), p);
        r = scanSelective(operationTree.getRight(), p, r);
        return r;
    }

    @Override
    public R visitGetTypeTree(GetTypeTree getTypeTree, P p) {
        return scan(getTypeTree.getExpression(), p);
    }
}
