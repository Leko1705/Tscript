package com.tscript.tscriptc.utils;

import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.TTree.*;

import java.util.List;
import java.util.Set;

/**
 * An Implementation of the {@link TreeFactory} interface.
 * Creates Instances of the {@link TTree} hierarchy.
 * @since 1.0
 * @author Lennart Köhler
 */
public class TreeMaker implements TreeFactory {

    @Override
    public ArgumentTree ArgumentTree(Location location, String name, ExpressionTree value) {
        return new TArgumentTree(location, name, value);
    }

    @Override
    public ArrayTree ArrayTree(Location location, List<? extends ExpressionTree> content) {
        return new TArrayTree(location, content);
    }

    @Override
    public AssignTree AssignTree(Location location,
                                 ExpressionTree assigned,
                                 ExpressionTree value) {
        return new TAssignTree(location, assigned, value);
    }

    @Override
    public BinaryOperationTree BinaryOperationTree(Location location,
                                                   ExpressionTree left,
                                                   ExpressionTree right,
                                                   Operation operation) {
        return new TBinaryOperationTree(location, left, right, operation);
    }

    @Override
    public BlockTree BlockTree(Location location, List<? extends StatementTree> statements) {
        return new TBlockTree(location, statements);
    }

    @Override
    public BooleanTree BooleanTree(Location location, boolean value) {
        return new TBooleanTree(location, value);
    }

    @Override
    public BreakTree BreakTree(Location location) {
        return new TBreakTree(location);
    }

    @Override
    public CallTree CallTree(Location location,
                             ExpressionTree called,
                             List<? extends ArgumentTree> arguments) {
        return new TCallTree(location, called, arguments);
    }

    @Override
    public ClassTree ClassTree(Location location,
                               ModifiersTree modifiers,
                               String name,
                               String superName,
                               ConstructorTree constructor,
                               List<? extends ClassMemberTree> members) {
        return new TClassTree(location, name, modifiers, superName, constructor, members);
    }

    @Override
    public ClosureTree ClosureTree(Location location,
                                   String name,
                                   ExpressionTree initializer) {
        return new TClosureTree(location, name, initializer);
    }

    @Override
    public ConstructorTree ConstructorTree(Location location,
                                           ModifiersTree modifiers,
                                           List<? extends ParameterTree> parameters,
                                           List<? extends ArgumentTree> superArguments,
                                           BlockTree body) {
        return new TConstructorTree(location, modifiers, parameters, superArguments, body);
    }

    @Override
    public ContainerAccessTree ContainerAccessTree(Location location,
                                                   ExpressionTree container,
                                                   ExpressionTree key) {
        return new TContainerAccessTree(location, container, key);
    }

    @Override
    public ContinueTree ContinueTree(Location location) {
        return new TContinueTree(location);
    }

    @Override
    public DictionaryTree DictionaryTree(Location location,
                                         List<? extends ExpressionTree> keys,
                                         List<? extends ExpressionTree> values) {
        return new TDictionaryTree(location, keys, values);
    }

    @Override
    public DoWhileTree DoWhileTree(Location location,
                                   StatementTree statement,
                                   ExpressionTree condition) {
        return new TDoWhileTree(location, statement, condition);
    }

    @Override
    public ExpressionStatementTree ExpressionStatementTree(Location location, ExpressionTree expression) {
        return new TExpressionStatementTree(location, expression);
    }

    @Override
    public FloatTree FloatTree(Location location, double value) {
        return new TFloatTree(location, value);
    }

    @Override
    public ForLoopTree ForLoopTree(Location location,
                                   VarDefTree runVar,
                                   ExpressionTree iterable,
                                   StatementTree statement) {
        return new TForLoopTree(location, runVar, iterable, statement);
    }

    @Override
    public FromImportTree FromImportTree(Location location,
                                         List<String> fromAccessChain,
                                         List<String> importAccessChain) {
        return new TFromImportTree(location, fromAccessChain, importAccessChain);
    }

    @Override
    public FunctionTree FunctionTree(Location location,
                                     ModifiersTree modifiers,
                                     String name,
                                     List<? extends ParameterTree> parameters,
                                     BlockTree body) {
        return new TFunctionTree(location, name, modifiers, parameters, body);
    }

    @Override
    public GetTypeTree GetTypeTree(Location location, ExpressionTree operand) {
        return new TGetTypeTree(location, operand);
    }

    @Override
    public IfElseTree IfElseTree(Location location,
                                 ExpressionTree condition,
                                 StatementTree thenStatement,
                                 StatementTree elseStatement) {
        return new TIfElseTree(location, condition, thenStatement, elseStatement);
    }

    @Override
    public ImportTree ImportTree(Location location, List<String> importAccessChain) {
        return new TImportTree(location, importAccessChain);
    }

    @Override
    public IntegerTree IntegerTree(Location location, int value) {
        return new TIntegerTree(location, value);
    }

    @Override
    public LambdaTree LambdaTree(Location location,
                                 List<? extends ClosureTree> closures,
                                 List<? extends ParameterTree> parameters,
                                 BlockTree body) {
        return new TLambdaTree(location, closures, parameters, body);
    }

    @Override
    public MemberAccessTree MemberAccessTree(Location location,
                                             ExpressionTree accessed,
                                             String memberName) {
        return new TMemberAccessTree(location, accessed, memberName);
    }

    @Override
    public ModifiersTree ModifiersTree(Location location, Set<Modifier> modifiers) {
        return new TModifiersTree(location, modifiers);
    }

    @Override
    public NamespaceTree NamespaceTree(Location location,
                                       ModifiersTree modifiers,
                                       String name,
                                       List<? extends DefinitionTree> definitions,
                                       List<? extends StatementTree> statements) {
        return new TNamespaceTree(location, name, modifiers, definitions, statements);
    }

    @Override
    public NotTree NotTree(Location location, ExpressionTree operand) {
        return new TNotTree(location, operand);
    }

    @Override
    public NullTree NullTree(Location location) {
        return new TNullTree(location);
    }

    @Override
    public ParameterTree ParameterTree(Location location,
                                       String name,
                                       ModifiersTree modifiers,
                                       ExpressionTree defaultValue) {
        return new TParameterTree(location, name, modifiers, defaultValue);
    }

    @Override
    public RangeTree RangeTree(Location location, ExpressionTree from, ExpressionTree to) {
        return new TRangeTree(location, from, to);
    }

    @Override
    public ReturnTree ReturnTree(Location location, ExpressionTree returned) {
        return new TReturnTree(location, returned);
    }

    @Override
    public RootTree RootTree(Location location,
                             List<? extends DefinitionTree> definitions,
                             List<? extends StatementTree> statements) {
        return new TRootTree(location, definitions, statements);
    }

    @Override
    public SignTree SignTree(Location location, boolean isNegation, ExpressionTree operand) {
        return new TSignTree(location, isNegation, operand);
    }

    @Override
    public StringTree StringTree(Location location, String value) {
        return new TStringTree(location, value);
    }

    @Override
    public ExpressionTree SuperTree(Location location, String name) {
        return new TSuperTree(location, name);
    }

    @Override
    public ThisTree ThisTree(Location location) {
        return new TThisTree(location);
    }

    @Override
    public ThrowTree ThrowTree(Location location, ExpressionTree thrown) {
        return new TThrowsTree(location, thrown);
    }

    @Override
    public TryCatchTree TryCatchTree(Location location,
                                     StatementTree tryBody,
                                     VarDefTree exceptionVar,
                                     StatementTree catchBody) {
        return new TTryCatchTree(location, tryBody, exceptionVar, catchBody);
    }

    @Override
    public UseTree UseTree(Location location, VariableTree variable) {
        return new TUseTree(location, variable);
    }

    @Override
    public VarDefsTree VarDefsTree(Location location,
                                   ModifiersTree modifiers,
                                   List<? extends VarDefTree> definedVars) {
        return new TVarDefsTree(location, modifiers, definedVars);
    }

    @Override
    public VarDefTree VarDefTree(Location location,
                                 String name,
                                 ExpressionTree initializer) {
        return new TVarDefTree(location, name, initializer);
    }

    @Override
    public VariableTree VariableTree(Location location, String name) {
        return new TVariableTree(location, name);
    }

    @Override
    public WhileDoTree WhileDoTree(Location location,
                                   ExpressionTree condition,
                                   StatementTree statement) {
        return new TWhileDoTree(location, condition, statement);
    }
}
