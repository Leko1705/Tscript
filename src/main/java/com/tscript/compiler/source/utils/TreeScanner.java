package com.tscript.compiler.source.utils;

import com.tscript.compiler.source.tree.*;

import java.util.List;

/**
 * A pre-implementation of the {@link TreeVisitor} interface, for
 * traversing a tree in-order.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
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
    public R visitArgument(ArgumentTree node, P p) {
        return scan(node.getExpression(), p);
    }

    @Override
    public R visitArray(ArrayTree node, P p) {
        return scan(node.getContents(), p);
    }

    @Override
    public R visitAssign(AssignTree node, P p) {
        R r = scan(node.getLeftOperand(), p);
        r = scanSelective(node.getRightOperand(), p, r);
        return r;
    }

    @Override
    public R visitBinaryOperation(BinaryOperationTree node, P p) {
        R r = scan(node.getLeftOperand(), p);
        r = scanSelective(node.getRightOperand(), p, r);
        return r;
    }

    @Override
    public R visitBlock(BlockTree node, P p) {
        return scan(node.getStatements(), p);
    }

    @Override
    public R visitBoolean(BooleanTree node, P p) {
        return null;
    }

    @Override
    public R visitBreak(BreakTree node, P p) {
        return null;
    }

    @Override
    public R visitCall(CallTree node, P p) {
        R r = scan(node.getCalled(), p);
        r = scanSelective(node.getArguments(), p, r);
        return r;
    }

    @Override
    public R visitClass(ClassTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanSelective(node.getMembers(), p, r);
        return r;
    }

    @Override
    public R visitClosure(ClosureTree node, P p) {
        return scan(node.getInitializer(), p);
    }

    @Override
    public R visitConstructor(ConstructorTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanSelective(node.getParameters(), p, r);
        r = scanSelective(node.getSuperArguments(), p, r);
        r = scanSelective(node.getBody(), p, r);
        return r;
    }

    @Override
    public R visitContainerAccess(ContainerAccessTree node, P p) {
        R r = scan(node.getContainer(), p);
        r = scanSelective(node.getKey(), p, r);
        return r;
    }

    @Override
    public R visitContinue(ContinueTree node, P p) {
        return null;
    }

    @Override
    public R visitDictionary(DictionaryTree node, P p) {
        R r = scan(node.getKeys(), p);
        r = scanSelective(node.getValues(), p, r);
        return r;
    }

    @Override
    public R visitDoWhileLoop(DoWhileTree node, P p) {
        R r = scan(node.getStatement(), p);
        r = scanSelective(node.getCondition(), p, r);
        return r;
    }

    @Override
    public R visitExpressionStatement(ExpressionStatementTree node, P p) {
        return scan(node.getExpression(), p);
    }

    @Override
    public R visitFloat(FloatTree node, P p) {
        return null;
    }

    @Override
    public R visitForLoop(ForLoopTree node, P p) {
        R r = scan(node.getVariable(), p);
        r = scanSelective(node.getIterable(), p, r);
        r = scanSelective(node.getStatement(), p, r);
        return r;
    }

    @Override
    public R visitFromImport(FromImportTree node, P p) {
        return null;
    }

    @Override
    public R visitFunction(FunctionTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanSelective(node.getParameters(), p, r);
        r = scanSelective(node.getBody(), p, r);
        return r;
    }

    @Override
    public R visitGetType(GetTypeTree node, P p) {
        return scan(node.getOperand(), p);
    }

    @Override
    public R visitIfElse(IfElseTree node, P p) {
        R r = scan(node.getCondition(), p);
        r = scanSelective(node.getThenStatement(), p, r);
        r = scanSelective(node.getElseStatement(), p, r);
        return r;
    }

    @Override
    public R visitImport(ImportTree node, P p) {
        return null;
    }

    @Override
    public R visitInteger(IntegerTree node, P p) {
        return null;
    }

    @Override
    public R visitIsTypeofTree(IsTypeofTree node, P p) {
        R r = scan(node.getChecked(), p);
        r = scanSelective(node.getType(), p, r);
        return r;
    }

    @Override
    public R visitLambda(LambdaTree node, P p) {
        R r = scan(node.getClosures(), p);
        r = scanSelective(node.getParameters(), p, r);
        r = scanSelective(node.getBody(), p, r);
        return r;
    }

    @Override
    public R visitMemberAccess(MemberAccessTree node, P p) {
        return scan(node.getExpression(), p);
    }

    @Override
    public R visitModifiers(ModifiersTree node, P p) {
        return null;
    }

    @Override
    public R visitNamespace(NamespaceTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanSelective(node.getDefinitions(), p, r);
        r = scanSelective(node.getStatements(), p, r);
        return r;
    }

    @Override
    public R visitNot(NotTree node, P p) {
        return scan(node.getOperand(), p);
    }

    @Override
    public R visitNull(NullTree node, P p) {
        return null;
    }

    @Override
    public R visitParameter(ParameterTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanSelective(node.getDefaultValue(), p, r);
        return r;
    }

    @Override
    public R visitRange(RangeTree node, P p) {
        R r = scan(node.getFrom(), p);
        r = scanSelective(node.getTo(), p, r);
        return r;
    }

    @Override
    public R visitReturn(ReturnTree node, P p) {
        return scan(node.getExpression(), p);
    }

    @Override
    public R visitRoot(RootTree node, P p) {
        R r = scan(node.getDefinitions(), p);
        r = scanSelective(node.getStatements(), p, r);
        return r;
    }

    @Override
    public R visitSign(SignTree node, P p) {
        return scan(node.getOperand(), p);
    }

    @Override
    public R visitString(StringTree node, P p) {
        return null;
    }

    @Override
    public R visitSuper(SuperTree node, P p) {
        return null;
    }

    @Override
    public R visitThis(ThisTree node, P p) {
        return null;
    }

    @Override
    public R visitThrow(ThrowTree node, P p) {
        return scan(node.getThrown(), p);
    }

    @Override
    public R visitTryCatch(TryCatchTree node, P p) {
        R r = scan(node.getTryStatement(), p);
        r = scanSelective(node.getExceptionVariable(), p, r);
        r = scanSelective(node.getCatchStatement(), p, r);
        return r;
    }

    @Override
    public R visitUse(UseTree node, P p) {
        return scan(node.getUsed(), p);
    }

    @Override
    public R visitVarDefs(VarDefsTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanSelective(node.getDefinitions(), p, r);
        return r;
    }

    @Override
    public R visitVarDef(VarDefTree node, P p) {
        return scan(node.getInitializer(), p);
    }

    @Override
    public R visitVariable(VariableTree node, P p) {
        return null;
    }

    @Override
    public R visitWhileDoLoop(WhileDoTree node, P p) {
        R r = scan(node.getCondition(), p);
        r = scanSelective(node.getStatement(), p, r);
        return r;
    }
}
