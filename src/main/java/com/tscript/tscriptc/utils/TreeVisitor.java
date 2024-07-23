package com.tscript.tscriptc.utils;

import com.tscript.tscriptc.tree.*;

/**
 * A visitor of trees, in the style of the visitor design pattern.
 * Classes implementing this interface are used to operate
 * on a tree when the kind of tree is unknown at compile time.
 * When a visitor is passed to a tree's {@link Tree#accept
 * accept} method, the <code>visit<i>Xyz</i></code> method most applicable
 * to that tree is invoked.
 *
 * <p> <b>WARNING:</b> It is possible that methods will be added to
 * this interface to accommodate new, currently unknown, language
 * structures added to future versions of the Tscript programming
 * language.  Therefore, visitor classes directly implementing this
 * interface may be source incompatible with future versions of the
 * platform.
 *
 * @param <R> the return type of this visitor's methods.  Use {@link
 *            Void} for visitors that do not need to return results.
 * @param <P> the type of the additional parameter to this visitor's
 *            methods.  Use {@code Void} for visitors that do not need an
 *            additional parameter.
 *
 * @author Lennart Köhler
 *
 * @since 1.6
 */
public interface TreeVisitor<P, R> {

    /**
     * Visits an {@code ArgumentTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitArgument(ArgumentTree node, P p);

    /**
     * Visits an {@code ArrayTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitArray(ArrayTree node, P p);

    /**
     * Visits an {@code AssignTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitAssign(AssignTree node, P p);

    /**
     * Visits a {@code BinaryOperationTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitBinaryOperation(BinaryOperationTree node, P p);

    /**
     * Visits a {@code BlockTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitBlock(BlockTree node, P p);

    /**
     * Visits a {@code BooleanTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitBoolean(BooleanTree node, P p);

    /**
     * Visits a {@code BreakTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitBreak(BreakTree node, P p);

    /**
     * Visits a {@code CallTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitCall(CallTree node, P p);

    /**
     * Visits a {@code ClassTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitClass(ClassTree node, P p);

    /**
     * Visits a {@code ClosureTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitClosure(ClosureTree node, P p);

    /**
     * Visits a {@code ConstructorTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitConstructor(ConstructorTree node, P p);

    /**
     * Visits a {@code ContainerAccessTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitContainerAccess(ContainerAccessTree node, P p);

    /**
     * Visits a {@code ContinueTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitContinue(ContinueTree node, P p);

    /**
     * Visits a {@code DictionaryTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitDictionary(DictionaryTree node, P p);

    /**
     * Visits a {@code DoWhileTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitDoWhileLoop(DoWhileTree node, P p);

    /**
     * Visits an {@code ExpressionStatementTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitExpressionStatement(ExpressionStatementTree node, P p);

    /**
     * Visits a {@code FloatTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitFloat(FloatTree node, P p);

    /**
     * Visits an {@code ForLoopTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitForLoop(ForLoopTree node, P p);

    /**
     * Visits a {@code FromImportTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitFromImport(FromImportTree node, P p);

    /**
     * Visits a {@code FunctionTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitFunction(FunctionTree node, P p);

    /**
     * Visits a {@code GetTypeTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitGetType(GetTypeTree node, P p);

    /**
     * Visits an {@code IfElseTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitIfElse(IfElseTree node, P p);

    /**
     * Visits an {@code ImportTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitImport(ImportTree node, P p);

    /**
     * Visits an {@code IntegerTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitInteger(IntegerTree node, P p);

    /**
     * Visits a {@code LambdaTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitLambda(LambdaTree node, P p);

    /**
     * Visits a {@code MemberAccessTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitMemberAccess(MemberAccessTree node, P p);

    /**
     * Visits a {@code ModifiersTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitModifiers(ModifiersTree node, P p);

    /**
     * Visits a {@code NamespaceTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitNamespace(NamespaceTree node, P p);

    /**
     * Visits a {@code NotTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitNot(NotTree node, P p);

    /**
     * Visits a {@code NullTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitNull(NullTree node, P p);

    /**
     * Visits a {@code ParameterTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitParameter(ParameterTree node, P p);

    /**
     * Visits a {@code RangeTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitRange(RangeTree node, P p);

    /**
     * Visits a {@code ReturnTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitReturn(ReturnTree node, P p);

    /**
     * Visits the {@code RootTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitRoot(RootTree node, P p);

    /**
     * Visits a {@code SignTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitSign(SignTree node, P p);

    /**
     * Visits a {@code StringTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitString(StringTree node, P p);

    /**
     * Visits a {@code SuperTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitSuper(SuperTree node, P p);

    /**
     * Visits a {@code ThisTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitThis(ThisTree node, P p);

    /**
     * Visits a {@code ThrowTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitThrow(ThrowTree node, P p);

    /**
     * Visits a {@code TryCatchTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitTryCatch(TryCatchTree node, P p);

    /**
     * Visits a {@code UseTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitUse(UseTree node, P p);

    /**
     * Visits a {@code VarDefsTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitVarDefs(VarDefsTree node, P p);

    /**
     * Visits a {@code VarDefTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitVarDef(VarDefTree node, P p);

    /**
     * Visits a {@code VariableTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitVariable(VariableTree node, P p);

    /**
     * Visits a {@code WhileDoTree} node.
     * @param node the node being visited
     * @param p a parameter value
     * @return a result value
     */
    R visitWhileDoLoop(WhileDoTree node, P p);

}
