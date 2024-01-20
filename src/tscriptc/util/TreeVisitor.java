package tscriptc.util;

import tscriptc.tree.*;

public interface TreeVisitor<P, R> {

    R visitRootTree(RootTree rootTree, P p);


    // definitions

    R visitNamespaceTree(NamespaceTree namespaceTree, P p);

    R visitClassTree(ClassTree classTree, P p);

    R visitConstructorTree(ConstructorTree constructorTree, P p);

    R visitFunctionTree(FunctionTree functionTree, P p);

    R visitParameterTree(ParameterTree parameterTree, P p);

    R visitNativeFunctionTree(NativeFunctionTree nativeFunctionTree, P p);

    R visitAbstractMethodTree(AbstractMethodTree abstractMethodTree, P p);


    // statements

    R visitImportTree(ImportTree importTree, P p);

    R visitUseTree(UseTree useTree, P p);

    R visitVarDecTree(VarDecTree varDecTree, P p);

    R visitIfElseTree(IfElseTree ifElseTree, P p);

    R visitWhileDoTree(WhileDoTree whileDoTree, P p);

    R visitDoWhileTree(DoWhileTree doWhileTree, P p);

    R visitForLoopTree(ForLoopTree forLoopTree, P p);

    R visitBreakTree(BreakTree breakTree, P p);

    R visitContinueTree(ContinueTree continueTree, P p);

    R visitReturnTree(ReturnTree returnTree, P p);

    R visitTryCatchTree(TryCatchTree tryCatchTree, P p);

    R visitThrowTree(ThrowTree throwTree, P p);

    R visitBlockTree(BlockTree blockTree, P p);

    R visitExpressionStatementTree(ExpressionStatementTree expressionStatementTree, P p);


    // expressions

    R visitIdentifierTree(IdentifierTree identifierTree, P p);

    R visitAssignTree(AssignTree assignTree, P p);

    R visitThisTree(ThisTree thisTree, P p);

    R visitSuperTree(SuperTree superTree, P p);

    R visitNullTree(NullLiteralTree tree, P p);

    R visitIntegerTree(IntegerLiteralTree tree, P p);

    R visitFloatTree(FloatLiteralTree tree, P p);

    R visitBooleanTree(BooleanLiteralTree tree, P p);

    R visitStringTree(StringLiteralTree tree, P p);

    R visitCallTree(CallTree callTree, P p);

    R visitArgumentTree(ArgumentTree argumentTree, P p);

    R visitRangeTree(RangeTree rangeTree, P p);

    R visitArrayTree(ArrayTree arrayTree, P p);

    R visitDictionaryTree(DictionaryTree dictionaryTree, P p);

    R visitContainerAccessTree(ContainerAccessTree accessTree, P p);

    R visitMemberAccessTree(MemberAccessTree accessTree, P p);

    R visitLambdaTree(LambdaTree lambdaTree, P p);

    R visitClosureTree(ClosureTree closureTree, P p);

    R visitNotTree(NotTree notTree, P p);

    R visitSignTree(SignTree signTree, P p);

    R visitOperationTree(BinaryOperationTree operationTree, P p);

    R visitGetTypeTree(GetTypeTree getTypeTree, P p);

}
