package com.tscript.compiler.impl.utils;

import java.util.List;
import com.tscript.compiler.impl.utils.TCTree.*;

/**
 * A pre-implementation of the {@link TCTree.Visitor} interface, for
 * traversing a tree in-order.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public class TCTreeScanner<P, R> implements TCTree.Visitor<P, R> {


    public R selectResult(R r1, R r2){
        return r1;
    }

    public R scan(TCTree tree, P p){
        return (tree != null) ? tree.accept(this, p) : null;
    }

    public R scan(List<? extends TCTree> trees, P p){
        R r = null;
        for (TCTree tree : trees)
            r = scanSelective(tree, p, r);
        return r;
    }

    public R scanSelective(TCTree tree, P p, R r){
        return selectResult(scan(tree, p), r);
    }

    public R scanSelective(List<? extends TCTree> trees, P p, R r){
        for (TCTree tree : trees)
            r = scanSelective(tree, p, r);
        return r;
    }



    @Override
    public R visitArgument(TCArgumentTree node, P p) {
        return scan(node.expression, p);
    }

    @Override
    public R visitArray(TCArrayTree node, P p) {
        return scan(node.content, p);
    }

    @Override
    public R visitAssign(TCAssignTree node, P p) {
        R r = scan(node.right, p);
        r = scanSelective(node.left, p, r);
        return r;
    }

    @Override
    public R visitBinaryOperation(TCBinaryOperationTree node, P p) {
        R r = scan(node.left, p);
        r = scanSelective(node.right, p, r);
        return r;
    }

    @Override
    public R visitBlock(TCBlockTree node, P p) {
        return scan(node.statements, p);
    }

    @Override
    public R visitBoolean(TCBooleanTree node, P p) {
        return null;
    }

    @Override
    public R visitBreak(TCBreakTree node, P p) {
        return null;
    }

    @Override
    public R visitCall(TCCallTree node, P p) {
        R r = scan(node.called, p);
        r = scanSelective(node.arguments, p, r);
        return r;
    }

    @Override
    public R visitCase(TCCaseTree node, P p) {
        return scan(node.statement, p);
    }

    @Override
    public R visitClass(TCClassTree node, P p) {
        R r = scan(node.modifiers, p);
        r = scanSelective(node.members, p, r);
        return r;
    }

    @Override
    public R visitClosure(TCClosureTree node, P p) {
        return scan(node.initializer, p);
    }

    @Override
    public R visitConstructor(TCConstructorTree node, P p) {
        R r = scan(node.modifiers, p);
        r = scanSelective(node.parameters, p, r);
        r = scanSelective(node.superArgs, p, r);
        r = scanSelective(node.body, p, r);
        return r;
    }

    @Override
    public R visitContainerAccess(TCContainerAccessTree node, P p) {
        R r = scan(node.container, p);
        r = scanSelective(node.key, p, r);
        return r;
    }

    @Override
    public R visitContinue(TCContinueTree node, P p) {
        return null;
    }

    @Override
    public R visitDictionary(TCDictionaryTree node, P p) {
        R r = scan(node.keys, p);
        r = scanSelective(node.values, p, r);
        return r;
    }

    @Override
    public R visitDoWhileLoop(TCDoWhileTree node, P p) {
        R r = scan(node.statement, p);
        r = scanSelective(node.condition, p, r);
        return r;
    }

    @Override
    public R visitExpressionStatement(TCExpressionStatementTree node, P p) {
        return scan(node.expression, p);
    }

    @Override
    public R visitFloat(TCFloatTree node, P p) {
        return null;
    }

    @Override
    public R visitForLoop(TCForLoopTree node, P p) {
        R r = scan(node.iterable, p);
        r = scanSelective(node.runVar, p, r);
        r = scanSelective(node.statement, p, r);
        return r;
    }

    @Override
    public R visitFromImport(TCFromImportTree node, P p) {
        return null;
    }

    @Override
    public R visitFunction(TCFunctionTree node, P p) {
        R r = scan(node.modifiers, p);
        r = scanSelective(node.parameters, p, r);
        r = scanSelective(node.body, p, r);
        return r;
    }

    @Override
    public R visitGetType(TCGetTypeTree node, P p) {
        return scan(node.operand, p);
    }

    @Override
    public R visitIfElse(TCIfElseTree node, P p) {
        R r = scan(node.condition, p);
        r = scanSelective(node.thenStatement, p, r);
        r = scanSelective(node.elseStatement, p, r);
        return r;
    }

    @Override
    public R visitImport(TCImportTree node, P p) {
        return null;
    }

    @Override
    public R visitInteger(TCIntegerTree node, P p) {
        return null;
    }

    @Override
    public R visitIsTypeof(TCIsTypeofTree node, P p) {
        R r = scan(node.checked, p);
        r = scanSelective(node.type, p, r);
        return r;
    }

    @Override
    public R visitLambda(TCLambdaTree node, P p) {
        R r = scan(node.closures, p);
        r = scanSelective(node.parameters, p, r);
        r = scanSelective(node.body, p, r);
        return r;
    }

    @Override
    public R visitMemberAccess(TCMemberAccessTree node, P p) {
        return scan(node.expression, p);
    }

    @Override
    public R visitModifiers(TCModifiersTree node, P p) {
        return null;
    }

    @Override
    public R visitNamespace(TCNamespaceTree node, P p) {
        R r = scan(node.modifiers, p);
        r = scanSelective(node.definitions, p, r);
        r = scanSelective(node.statements, p, r);
        return r;
    }

    @Override
    public R visitNot(TCNotTree node, P p) {
        return scan(node.operand, p);
    }

    @Override
    public R visitNull(TCNullTree node, P p) {
        return null;
    }

    @Override
    public R visitParameter(TCParameterTree node, P p) {
        R r = scan(node.modifiers, p);
        r = scanSelective(node.defaultValue, p, r);
        return r;
    }

    @Override
    public R visitRange(TCRangeTree node, P p) {
        R r = scan(node.from, p);
        r = scanSelective(node.to, p, r);
        return r;
    }

    @Override
    public R visitReturn(TCReturnTree node, P p) {
        return scan(node.returned, p);
    }

    @Override
    public R visitRoot(TCRootTree node, P p) {
        R r = scan(node.imports, p);
        r = scanSelective(node.definitions, p, r);
        r = scanSelective(node.statements, p, r);
        return r;
    }

    @Override
    public R visitSign(TCSignTree node, P p) {
        return scan(node.operand, p);
    }

    @Override
    public R visitString(TCStringTree node, P p) {
        return null;
    }

    @Override
    public R visitSuper(TCSuperTree node, P p) {
        return null;
    }

    @Override
    public R visitSwitch(TCSwitchTree node, P p) {
        R r = scan(node.expression, p);
        r = scanSelective(node.cases, p, r);
        r = scanSelective(node.defaultCase, p, r);
        return r;
    }

    @Override
    public R visitThis(TCThisTree node, P p) {
        return null;
    }

    @Override
    public R visitThrow(TCThrowTree node, P p) {
        return scan(node.thrown, p);
    }

    @Override
    public R visitTryCatch(TCTryCatchTree node, P p) {
        R r = scan(node.tryStatement, p);
        r = scanSelective(node.exceptionVar, p, r);
        r = scanSelective(node.catchStatement, p, r);
        return r;
    }

    @Override
    public R visitUse(TCUseTree node, P p) {
        return scan(node.used, p);
    }

    @Override
    public R visitVarDefs(TCVarDefsTree node, P p) {
        R r = scan(node.modifiers, p);
        r = scanSelective(node.definitions, p, r);
        return r;
    }

    @Override
    public R visitVarDef(TCVarDefTree node, P p) {
        return scan(node.initializer, p);
    }

    @Override
    public R visitVariable(TCVariableTree node, P p) {
        return null;
    }

    @Override
    public R visitWhileDoLoop(TCWhileDoTree node, P p) {
        R r = scan(node.condition, p);
        r = scanSelective(node.statement, p, r);
        return r;
    }
}
