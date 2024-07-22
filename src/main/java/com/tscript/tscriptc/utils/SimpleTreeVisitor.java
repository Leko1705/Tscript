package com.tscript.tscriptc.utils;

import com.tscript.tscriptc.tree.*;

/**
 * A {@link TreeVisitor} with pre-implemented methods. Performs a
 * {@link #defaultAction(Tree, Object)} by default.
 * 
 * @since 1.0
 * @author Lennart Köhler
 */
public class SimpleTreeVisitor<P, R> implements TreeVisitor<P, R> {
    
    private final R defaultValue;


    public SimpleTreeVisitor(R defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public SimpleTreeVisitor(){
        this(null);
    }
    

    public R defaultAction(Tree node, P p){
        return defaultValue;
    }
    
    @Override
    public R visitArgument(ArgumentTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitArray(ArrayTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitAssign(AssignTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitBinaryOperation(BinaryOperationTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitBlock(BlockTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitBoolean(BooleanTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitBreak(BreakTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitCall(CallTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitClass(ClassTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitClosure(ClosureTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitConstructor(ConstructorTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitContainerAccess(ContainerAccessTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitContinue(ContinueTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitDictionary(DictionaryTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitDoWhileLoop(DoWhileTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitExpressionStatement(ExpressionStatementTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitFloat(FloatTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitForLoop(ForLoopTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitFromImport(FromImportTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitFunction(FunctionTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitGetType(GetTypeTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitIfElse(IfElseTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitImport(ImportTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitInteger(IntegerTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitLambda(LambdaTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitMemberAccess(MemberAccessTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitModifiers(ModifiersTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitNamespace(NamespaceTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitNot(NotTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitNull(NullTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitParameter(ParameterTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitRange(RangeTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitReturn(ReturnTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitRoot(RootTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitSign(SignTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitString(StringTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitThis(ThisTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitThrow(ThrowTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitTryCatch(TryCatchTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitUse(UseTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitVarDefs(VarDefsTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitVarDef(VarDefTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitVariable(VariableTree node, P p) {
        return defaultAction(node, p);
    }

    @Override
    public R visitWhileDoLoop(WhileDoTree node, P p) {
        return defaultAction(node, p);
    }
    
}
