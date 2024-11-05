package com.tscript.compiler.impl.utils;

import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.impl.utils.TCTree.*;
import com.tscript.compiler.source.utils.Location;

import java.util.List;
import java.util.Set;

/**
 * An Implementation of the {@link TCTree.Factory} interface.
 * Creates Instances of the {@link TCTree} hierarchy.
 * @since 1.0
 * @author Lennart Köhler
 */
public class TreeMaker implements TCTree.Factory {

    @Override
    public TCArgumentTree ArgumentTree(Location location, String name, TCExpressionTree value) {
        return new TCArgumentTree(location, name, value);
    }

    @Override
    public TCArrayTree ArrayTree(Location location, List<? extends TCExpressionTree> content) {
        return new TCArrayTree(location, content);
    }

    @Override
    public TCAssignTree AssignTree(Location location,
                                   TCExpressionTree assigned,
                                   TCExpressionTree value) {
        return new TCAssignTree(location, assigned, value);
    }

    @Override
    public TCBinaryOperationTree BinaryOperationTree(Location location,
                                                     TCExpressionTree left,
                                                     TCExpressionTree right,
                                                     Operation operation) {
        return new TCBinaryOperationTree(location, left, right, operation);
    }

    @Override
    public TCBlockTree BlockTree(Location location, List<? extends TCStatementTree> statements) {
        return new TCBlockTree(location, statements);
    }

    @Override
    public TCBooleanTree BooleanTree(Location location, boolean value) {
        return new TCBooleanTree(location, value);
    }

    @Override
    public TCBreakTree BreakTree(Location location) {
        return new TCBreakTree(location);
    }

    @Override
    public TCCallTree CallTree(Location location,
                               TCExpressionTree called,
                             List<? extends TCArgumentTree> arguments) {
        return new TCCallTree(location, called, arguments);
    }

    @Override
    public TCCaseTree CaseTree(Location location,
                               TCStatementTree statementTree,
                               boolean allowBreak) {
        return new TCCaseTree(location, statementTree, allowBreak);
    }

    @Override
    public TCClassTree ClassTree(Location location,
                                 TCModifiersTree modifiers,
                                 String name,
                                 List<String> superName,
                                 List<? extends TCTree> members) {
        return new TCClassTree(location, name, modifiers, superName, members);
    }

    @Override
    public TCClosureTree ClosureTree(Location location,
                                   String name,
                                     TCExpressionTree initializer) {
        return new TCClosureTree(location, name, initializer);
    }

    @Override
    public TCConstructorTree ConstructorTree(Location location,
                                             TCModifiersTree modifiers,
                                           List<? extends TCParameterTree> parameters,
                                           List<? extends TCArgumentTree> superArguments,
                                             TCBlockTree body) {
        return new TCConstructorTree(location, modifiers, parameters, superArguments, body);
    }

    @Override
    public TCContainerAccessTree ContainerAccessTree(Location location,
                                                     TCExpressionTree container,
                                                     TCExpressionTree key) {
        return new TCContainerAccessTree(location, container, key);
    }

    @Override
    public TCContinueTree ContinueTree(Location location) {
        return new TCContinueTree(location);
    }

    @Override
    public TCDictionaryTree DictionaryTree(Location location,
                                         List<? extends TCExpressionTree> keys,
                                         List<? extends TCExpressionTree> values) {
        return new TCDictionaryTree(location, keys, values);
    }

    @Override
    public TCDoWhileTree DoWhileTree(Location location,
                                     TCStatementTree statement,
                                     TCExpressionTree condition) {
        return new TCDoWhileTree(location, statement, condition);
    }

    @Override
    public TCExpressionStatementTree ExpressionStatementTree(Location location, TCExpressionTree expression) {
        return new TCExpressionStatementTree(location, expression);
    }

    @Override
    public TCFloatTree FloatTree(Location location, double value) {
        return new TCFloatTree(location, value);
    }

    @Override
    public TCForLoopTree ForLoopTree(Location location,
                                     TCVarDefTree runVar,
                                     TCExpressionTree iterable,
                                     TCStatementTree statement) {
        return new TCForLoopTree(location, runVar, iterable, statement);
    }

    @Override
    public TCFromImportTree FromImportTree(Location location,
                                         List<String> fromAccessChain,
                                         List<String> importAccessChain) {
        return new TCFromImportTree(location, fromAccessChain, importAccessChain);
    }

    @Override
    public TCFunctionTree FunctionTree(Location location,
                                       TCModifiersTree modifiers,
                                       String name,
                                       List<? extends TCParameterTree> parameters,
                                       TCBlockTree body) {
        return new TCFunctionTree(location, name, modifiers, parameters, body);
    }

    @Override
    public TCGetTypeTree GetTypeTree(Location location, TCExpressionTree operand) {
        return new TCGetTypeTree(location, operand);
    }

    @Override
    public TCIfElseTree IfElseTree(Location location,
                                   TCExpressionTree condition,
                                   TCStatementTree thenStatement,
                                   TCStatementTree elseStatement) {
        return new TCIfElseTree(location, condition, thenStatement, elseStatement);
    }

    @Override
    public TCImportTree ImportTree(Location location, List<String> importAccessChain) {
        return new TCImportTree(location, importAccessChain);
    }

    @Override
    public TCIntegerTree IntegerTree(Location location, int value) {
        return new TCIntegerTree(location, value);
    }

    @Override
    public TCIsTypeofTree IsTypeofTree(Location location, TCExpressionTree checked, TCExpressionTree type) {
        return new TCIsTypeofTree(location, checked, type);
    }

    @Override
    public TCLambdaTree LambdaTree(Location location,
                                   List<? extends TCClosureTree> closures,
                                   List<? extends TCParameterTree> parameters,
                                   TCBlockTree body) {
        return new TCLambdaTree(location, closures, parameters, body);
    }

    @Override
    public TCMemberAccessTree MemberAccessTree(Location location,
                                               TCExpressionTree accessed,
                                               String memberName) {
        return new TCMemberAccessTree(location, accessed, memberName);
    }

    @Override
    public TCModifiersTree ModifiersTree(Location location, Set<Modifier> modifiers) {
        return new TCModifiersTree(location, modifiers);
    }

    @Override
    public TCNamespaceTree NamespaceTree(Location location,
                                         TCModifiersTree modifiers,
                                       String name,
                                       List<? extends TCTree> definitions,
                                       List<? extends TCStatementTree> statements) {
        return new TCNamespaceTree(location, name, modifiers, definitions, statements);
    }

    @Override
    public TCNotTree NotTree(Location location, TCExpressionTree operand) {
        return new TCNotTree(location, operand);
    }

    @Override
    public TCNullTree NullTree(Location location) {
        return new TCNullTree(location);
    }

    @Override
    public TCParameterTree ParameterTree(Location location,
                                         String name,
                                         TCModifiersTree modifiers,
                                         TCExpressionTree defaultValue) {
        return new TCParameterTree(location, name, modifiers, defaultValue);
    }

    @Override
    public TCRangeTree RangeTree(Location location, TCExpressionTree from, TCExpressionTree to) {
        return new TCRangeTree(location, from, to);
    }

    @Override
    public TCReturnTree ReturnTree(Location location, TCExpressionTree returned) {
        return new TCReturnTree(location, returned);
    }

    @Override
    public TCRootTree RootTree(Location location,
                             String moduleName,
                             List<? extends TCTree> imports,
                             List<? extends TCDefinitionTree> definitions,
                             List<? extends TCStatementTree> statements) {
        return new TCRootTree(location, moduleName, imports, definitions, statements);
    }

    @Override
    public TCSignTree SignTree(Location location, boolean isNegation, TCExpressionTree operand) {
        return new TCSignTree(location, isNegation, operand);
    }

    @Override
    public TCStringTree StringTree(Location location, String value) {
        return new TCStringTree(location, value);
    }

    @Override
    public TCSuperTree SuperTree(Location location, String name) {
        return new TCSuperTree(location, name);
    }

    @Override
    public TCSwitchTree SwitchTree(Location location, List<? extends TCCaseTree> cases, TCStatementTree defaultCase) {
        return new TCSwitchTree(location, cases, defaultCase);
    }

    @Override
    public TCThisTree ThisTree(Location location) {
        return new TCThisTree(location);
    }

    @Override
    public TCThrowTree ThrowTree(Location location, TCExpressionTree thrown) {
        return new TCThrowTree(location, thrown);
    }

    @Override
    public TCTryCatchTree TryCatchTree(Location location,
                                       TCStatementTree tryBody,
                                       TCVarDefTree exceptionVar,
                                       TCStatementTree catchBody) {
        return new TCTryCatchTree(location, tryBody, exceptionVar, catchBody);
    }

    @Override
    public TCUseTree UseTree(Location location, TCExpressionTree variable, String name) {
        return new TCUseTree(location, variable, name);
    }

    @Override
    public TCVarDefsTree VarDefsTree(Location location,
                                     TCModifiersTree modifiers,
                                     List<? extends TCVarDefTree> definedVars) {
        return new TCVarDefsTree(location, modifiers, definedVars);
    }

    @Override
    public TCVarDefTree VarDefTree(Location location,
                                   String name,
                                   TCExpressionTree initializer) {
        return new TCVarDefTree(location, name, initializer);
    }

    @Override
    public TCVariableTree VariableTree(Location location, String name) {
        return new TCVariableTree(location, name);
    }

    @Override
    public TCWhileDoTree WhileDoTree(Location location,
                                     TCExpressionTree condition,
                                     TCStatementTree statement) {
        return new TCWhileDoTree(location, condition, statement);
    }
}
