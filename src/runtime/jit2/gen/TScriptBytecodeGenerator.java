package runtime.jit2.gen;

import runtime.jit2.graph.BranchedLink;
import runtime.jit2.graph.CFGNode;
import runtime.jit2.graph.CFGVisitor;
import runtime.jit2.graph.SimpleLink;
import runtime.type.Callable;
import tscriptc.tree.*;
import tscriptc.util.TreeVisitor;

public class TScriptBytecodeGenerator
        implements Generator, CFGVisitor<Void, Void>, TreeVisitor<Void, Void> {





    @Override
    public Callable generate(CFGNode node) {
        return null;
    }



    @Override
    public Void visitSimpleLink(SimpleLink link, Void unused) {
        return null;
    }

    @Override
    public Void visitBranchLink(BranchedLink link, Void unused) {
        return null;
    }



    @Override
    public Void visitRootTree(RootTree rootTree, Void unused) {
        return null;
    }

    @Override
    public Void visitNamespaceTree(NamespaceTree namespaceTree, Void unused) {
        return null;
    }

    @Override
    public Void visitClassTree(ClassTree classTree, Void unused) {
        return null;
    }

    @Override
    public Void visitConstructorTree(ConstructorTree constructorTree, Void unused) {
        return null;
    }

    @Override
    public Void visitFunctionTree(FunctionTree functionTree, Void unused) {
        return null;
    }

    @Override
    public Void visitParameterTree(ParameterTree parameterTree, Void unused) {
        return null;
    }

    @Override
    public Void visitNativeFunctionTree(NativeFunctionTree nativeFunctionTree, Void unused) {
        return null;
    }

    @Override
    public Void visitAbstractMethodTree(AbstractMethodTree abstractMethodTree, Void unused) {
        return null;
    }

    @Override
    public Void visitImportTree(ImportTree importTree, Void unused) {
        return null;
    }

    @Override
    public Void visitUseTree(UseTree useTree, Void unused) {
        return null;
    }

    @Override
    public Void visitVarDecTree(VarDecTree varDecTree, Void unused) {
        return null;
    }

    @Override
    public Void visitMultiVarDecTree(MultiVarDecTree varDecTrees, Void unused) {
        return null;
    }

    @Override
    public Void visitIfElseTree(IfElseTree ifElseTree, Void unused) {
        return null;
    }

    @Override
    public Void visitWhileDoTree(WhileDoTree whileDoTree, Void unused) {
        return null;
    }

    @Override
    public Void visitDoWhileTree(DoWhileTree doWhileTree, Void unused) {
        return null;
    }

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Void unused) {
        return null;
    }

    @Override
    public Void visitBreakTree(BreakTree breakTree, Void unused) {
        return null;
    }

    @Override
    public Void visitContinueTree(ContinueTree continueTree, Void unused) {
        return null;
    }

    @Override
    public Void visitReturnTree(ReturnTree returnTree, Void unused) {
        return null;
    }

    @Override
    public Void visitTryCatchTree(TryCatchTree tryCatchTree, Void unused) {
        return null;
    }

    @Override
    public Void visitThrowTree(ThrowTree throwTree, Void unused) {
        return null;
    }

    @Override
    public Void visitBlockTree(BlockTree blockTree, Void unused) {
        return null;
    }

    @Override
    public Void visitExpressionStatementTree(ExpressionStatementTree expressionStatementTree, Void unused) {
        return null;
    }

    @Override
    public Void visitIdentifierTree(IdentifierTree identifierTree, Void unused) {
        return null;
    }

    @Override
    public Void visitAssignTree(AssignTree assignTree, Void unused) {
        return null;
    }

    @Override
    public Void visitThisTree(ThisTree thisTree, Void unused) {
        return null;
    }

    @Override
    public Void visitSuperTree(SuperTree superTree, Void unused) {
        return null;
    }

    @Override
    public Void visitNullTree(NullLiteralTree tree, Void unused) {
        return null;
    }

    @Override
    public Void visitIntegerTree(IntegerLiteralTree tree, Void unused) {
        return null;
    }

    @Override
    public Void visitFloatTree(FloatLiteralTree tree, Void unused) {
        return null;
    }

    @Override
    public Void visitBooleanTree(BooleanLiteralTree tree, Void unused) {
        return null;
    }

    @Override
    public Void visitStringTree(StringLiteralTree tree, Void unused) {
        return null;
    }

    @Override
    public Void visitCallTree(CallTree callTree, Void unused) {
        return null;
    }

    @Override
    public Void visitArgumentTree(ArgumentTree argumentTree, Void unused) {
        return null;
    }

    @Override
    public Void visitRangeTree(RangeTree rangeTree, Void unused) {
        return null;
    }

    @Override
    public Void visitArrayTree(ArrayTree arrayTree, Void unused) {
        return null;
    }

    @Override
    public Void visitDictionaryTree(DictionaryTree dictionaryTree, Void unused) {
        return null;
    }

    @Override
    public Void visitContainerAccessTree(ContainerAccessTree accessTree, Void unused) {
        return null;
    }

    @Override
    public Void visitMemberAccessTree(MemberAccessTree accessTree, Void unused) {
        return null;
    }

    @Override
    public Void visitLambdaTree(LambdaTree lambdaTree, Void unused) {
        return null;
    }

    @Override
    public Void visitClosureTree(ClosureTree closureTree, Void unused) {
        return null;
    }

    @Override
    public Void visitNotTree(NotTree notTree, Void unused) {
        return null;
    }

    @Override
    public Void visitSignTree(SignTree signTree, Void unused) {
        return null;
    }

    @Override
    public Void visitOperationTree(BinaryOperationTree operationTree, Void unused) {
        return null;
    }

    @Override
    public Void visitGetTypeTree(GetTypeTree getTypeTree, Void unused) {
        return null;
    }

    @Override
    public Void visitBreakPointTree(BreakPointTree bpTree, Void unused) {
        return null;
    }


}
