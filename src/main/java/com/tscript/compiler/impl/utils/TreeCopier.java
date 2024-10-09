package com.tscript.compiler.impl.utils;

import com.tscript.compiler.impl.utils.TCTree.*;

import java.util.ArrayList;
import java.util.List;

public class TreeCopier extends TCTreeScanner<Void, TCTree> {

    private final TreeMaker F = new TreeMaker();

    @SuppressWarnings("unchecked")
    private <T extends TCTree> T copy(T tree){
        return (T) scan(tree, null);
    }

    @SuppressWarnings("unchecked")
    private <T extends TCTree> List<T> copy(List<T> trees){
        List<T> copies = new ArrayList<>();
        for (TCTree tree : trees) {
            copies.add((T) copy(tree));
        }
        return copies;
    }

    @Override
    public TCArgumentTree visitArgument(TCArgumentTree node, Void unused) {
        return F.ArgumentTree(node.getLocation(), node.getName(), copy(node.expression));
    }

    @Override
    public TCArrayTree visitArray(TCArrayTree node, Void unused) {
        return F.ArrayTree(node.getLocation(), copy(node.content));
    }

    @Override
    public TCAssignTree visitAssign(TCAssignTree node, Void unused) {
        return F.AssignTree(
                node.getLocation(),
                copy(node.left),
                copy(node.right));
    }

    @Override
    public TCBinaryOperationTree visitBinaryOperation(TCBinaryOperationTree node, Void unused) {
        return F.BinaryOperationTree(
                node.getLocation(),
                copy(node.left),
                copy(node.right),
                node.getOperationType());
    }

    @Override
    public TCBlockTree visitBlock(TCBlockTree node, Void unused) {
        return F.BlockTree(node.getLocation(), copy(node.statements));
    }

    @Override
    public TCBooleanTree visitBoolean(TCBooleanTree node, Void unused) {
        return F.BooleanTree(node.getLocation(), node.get());
    }

    @Override
    public TCBreakTree visitBreak(TCBreakTree node, Void unused) {
        return F.BreakTree(node.getLocation());
    }

    @Override
    public TCCallTree visitCall(TCCallTree node, Void unused) {
        return F.CallTree(
                node.getLocation(),
                copy(node.called),
                copy(node.arguments));
    }

    @Override
    public TCClassTree visitClass(TCClassTree node, Void unused) {
        return F.ClassTree(
                node.getLocation(),
                copy(node.modifiers),
                node.getName(),
                node.getSuper(),
                copy(node.members));
    }

    @Override
    public TCClosureTree visitClosure(TCClosureTree node, Void unused) {
        return F.ClosureTree(node.getLocation(), node.getName(), copy(node.initializer));
    }

    @Override
    public TCConstructorTree visitConstructor(TCConstructorTree node, Void unused) {
        return F.ConstructorTree(
                node.getLocation(),
                copy(node.modifiers),
                copy(node.parameters),
                copy(node.superArgs),
                copy(node.body));
    }

    @Override
    public TCContainerAccessTree visitContainerAccess(TCContainerAccessTree node, Void unused) {
        return F.ContainerAccessTree(node.getLocation(), copy(node.container), copy(node.key));
    }

    @Override
    public TCContinueTree visitContinue(TCContinueTree node, Void unused) {
        return F.ContinueTree(node.getLocation());
    }

    @Override
    public TCDictionaryTree visitDictionary(TCDictionaryTree node, Void unused) {
        return F.DictionaryTree(node.getLocation(), copy(node.keys), copy(node.values));
    }

    @Override
    public TCDoWhileTree visitDoWhileLoop(TCDoWhileTree node, Void unused) {
        return F.DoWhileTree(node.getLocation(), copy(node.statement), copy(node.condition));
    }

    @Override
    public TCExpressionStatementTree visitExpressionStatement(TCExpressionStatementTree node, Void unused) {
        return F.ExpressionStatementTree(node.getLocation(), copy(node.expression));
    }

    @Override
    public TCFloatTree visitFloat(TCFloatTree node, Void unused) {
        return F.FloatTree(node.getLocation(), node.get());
    }

    @Override
    public TCForLoopTree visitForLoop(TCForLoopTree node, Void unused) {
        return F.ForLoopTree(
                node.getLocation(),
                copy(node.runVar),
                copy(node.iterable),
                copy(node.statement));
    }

    @Override
    public TCFromImportTree visitFromImport(TCFromImportTree node, Void unused) {
        return F.FromImportTree(node.getLocation(), node.getFromAccessChain(), node.getImportAccessChain());
    }

    @Override
    public TCFunctionTree visitFunction(TCFunctionTree node, Void unused) {
        return F.FunctionTree(
                node.getLocation(),
                copy(node.modifiers),
                node.getName(),
                copy(node.parameters),
                copy(node.body));
    }

    @Override
    public TCGetTypeTree visitGetType(TCGetTypeTree node, Void unused) {
        return F.GetTypeTree(node.getLocation(), copy(node.operand));
    }

    @Override
    public TCIfElseTree visitIfElse(TCIfElseTree node, Void unused) {
        return F.IfElseTree(
                node.getLocation(),
                copy(node.condition),
                copy(node.thenStatement),
                copy(node.elseStatement));
    }

    @Override
    public TCImportTree visitImport(TCImportTree node, Void unused) {
        return F.ImportTree(node.getLocation(), node.getAccessChain());
    }

    @Override
    public TCIntegerTree visitInteger(TCIntegerTree node, Void unused) {
        return F.IntegerTree(node.getLocation(), node.get());
    }

    @Override
    public TCLambdaTree visitLambda(TCLambdaTree node, Void unused) {
        return F.LambdaTree(
                node.getLocation(),
                copy(node.closures),
                copy(node.parameters),
                copy(node.body));
    }

    @Override
    public TCMemberAccessTree visitMemberAccess(TCMemberAccessTree node, Void unused) {
        return F.MemberAccessTree(node.getLocation(), copy(node.expression), node.getMemberName());
    }

    @Override
    public TCModifiersTree visitModifiers(TCModifiersTree node, Void unused) {
        return F.ModifiersTree(node.getLocation(), node.getFlags());
    }

    @Override
    public TCNamespaceTree visitNamespace(TCNamespaceTree node, Void unused) {
        return F.NamespaceTree(
                node.getLocation(),
                copy(node.modifiers),
                node.getName(),
                copy(node.definitions),
                copy(node.statements));
    }

    @Override
    public TCNotTree visitNot(TCNotTree node, Void unused) {
        return F.NotTree(node.getLocation(), copy(node.operand));
    }

    @Override
    public TCNullTree visitNull(TCNullTree node, Void unused) {
        return F.NullTree(node.getLocation());
    }

    @Override
    public TCParameterTree visitParameter(TCParameterTree node, Void unused) {
        return F.ParameterTree(node.getLocation(), node.getName(), copy(node.modifiers), copy(node.defaultValue));
    }

    @Override
    public TCRangeTree visitRange(TCRangeTree node, Void unused) {
        return F.RangeTree(node.getLocation(), copy(node.from), copy(node.to));
    }

    @Override
    public TCReturnTree visitReturn(TCReturnTree node, Void unused) {
        return F.ReturnTree(node.getLocation(), copy(node.returned));
    }

    @Override
    public TCRootTree visitRoot(TCRootTree node, Void unused) {
        return F.RootTree(
                node.getLocation(),
                node.moduleName,
                node.imports,
                copy(node.definitions),
                copy(node.statements));
    }

    @Override
    public TCSignTree visitSign(TCSignTree node, Void unused) {
        return F.SignTree(node.getLocation(), node.isNegation(), copy(node.operand));
    }

    @Override
    public TCStringTree visitString(TCStringTree node, Void unused) {
        return F.StringTree(node.getLocation(), node.get());
    }

    @Override
    public TCSuperTree visitSuper(TCSuperTree node, Void unused) {
        return F.SuperTree(node.getLocation(), node.getName());
    }

    @Override
    public TCThisTree visitThis(TCThisTree node, Void unused) {
        return F.ThisTree(node.getLocation());
    }

    @Override
    public TCThrowTree visitThrow(TCThrowTree node, Void unused) {
        return F.ThrowTree(node.getLocation(), copy(node.thrown));
    }

    @Override
    public TCTryCatchTree visitTryCatch(TCTryCatchTree node, Void unused) {
        return F.TryCatchTree(
                node.getLocation(),
                copy(node.tryStatement),
                copy(node.exceptionVar),
                copy(node.catchStatement));
    }

    @Override
    public TCUseTree visitUse(TCUseTree node, Void unused) {
        return F.UseTree(node.getLocation(), copy(node.varTree));
    }

    @Override
    public TCVarDefsTree visitVarDefs(TCVarDefsTree node, Void unused) {
        return F.VarDefsTree(node.getLocation(), copy(node.modifiers), copy(node.definitions));
    }

    @Override
    public TCVarDefTree visitVarDef(TCVarDefTree node, Void unused) {
        return F.VarDefTree(node.getLocation(), node.getName(), copy(node.initializer));
    }

    @Override
    public TCVariableTree visitVariable(TCVariableTree node, Void unused) {
        return F.VariableTree(node.getLocation(), node.getName());
    }

    @Override
    public TCWhileDoTree visitWhileDoLoop(TCWhileDoTree node, Void unused) {
        return F.WhileDoTree(node.getLocation(), copy(node.condition), copy(node.statement));
    }
}
