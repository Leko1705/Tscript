package com.tscript.tscriptc.utils;

import com.tscript.tscriptc.tree.*;

import java.util.List;
import java.util.Set;

/**
 * A class for default implementations for Tee Nodes.
 * @since 1.0
 * @author Lennart Köhler
 */
public abstract class TTree implements Tree {

    private final Location location;

    private TTree(Location location){
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public static class TArgumentTree extends TTree implements ArgumentTree {
        public final String name;
        public final ExpressionTree expression;
        public TArgumentTree(Location location, String name, ExpressionTree expression) {
            super(location);
            this.name = name;
            this.expression = expression;
        }
        @Override public String getName() { return name; }
        @Override public ExpressionTree getExpression() { return expression; }
    }

    public static class TArrayTree extends TTree implements ArrayTree {
        public final List<? extends ExpressionTree> content;
        public TArrayTree(Location location, List<? extends ExpressionTree> content) {
            super(location);
            this.content = content;
        }
        @Override public List<? extends ExpressionTree> getContents() { return content; }
    }

    private abstract static class TBinaryTree extends TTree implements BinaryExpressionTree {
        public final ExpressionTree left, right;
        public TBinaryTree(Location location, ExpressionTree left, ExpressionTree right) {
            super(location);
            this.left = left;
            this.right = right;
        }
        @Override public ExpressionTree getLeftOperand() { return left; }
        @Override public ExpressionTree getRightOperand() { return right; }
    }

    public static class TAssignTree extends TBinaryTree implements AssignTree {
        public TAssignTree(Location location, ExpressionTree left, ExpressionTree right) {
            super(location, left, right);
        }
    }

    public static class TBinaryOperationTree extends TBinaryTree implements BinaryOperationTree {
        public final Operation operation;
        public TBinaryOperationTree(Location location, ExpressionTree left, ExpressionTree right, Operation operation) {
            super(location, left, right);
            this.operation = operation;
        }
        @Override public Operation getOperationType() { return operation; }
    }

    public static class TBlockTree extends TTree implements BlockTree {
        public final List<? extends StatementTree> statements;
        public TBlockTree(Location location, List<? extends StatementTree> statements) {
            super(location);
            this.statements = statements;
        }
        @Override public List<? extends StatementTree> getStatements() { return statements; }
    }

    private abstract static class TLiteralTree<T> extends TTree implements LiteralTree<T> {
        public final T value;
        private TLiteralTree(Location location, T value) {
            super(location);
            this.value = value;
        }
        @Override public T get() { return value; }
    }

    public static class TBooleanTree extends TLiteralTree<Boolean> implements BooleanTree {
        public TBooleanTree(Location location, Boolean value) {
            super(location, value);
        }
    }

    public static class TBreakTree extends TTree implements BreakTree {
        public TBreakTree(Location location) {
            super(location);
        }
    }

    public static class TCallTree extends TTree implements CallTree {
        public final ExpressionTree called;
        public final List<? extends ArgumentTree> arguments;
        public TCallTree(Location location, ExpressionTree called, List<? extends ArgumentTree> arguments) {
            super(location);
            this.called = called;
            this.arguments = arguments;
        }
        @Override public ExpressionTree getCalled() { return called; }
        @Override public List<? extends ArgumentTree> getArguments() { return arguments; }
    }

    private static abstract class TDefinitionTree extends TTree implements DefinitionTree {
        public final String name;
        public final ModifiersTree modifiers;
        private TDefinitionTree(Location location, String name, ModifiersTree modifiers) {
            super(location);
            this.name = name;
            this.modifiers = modifiers;
        }
        @Override public ModifiersTree getModifiers() { return modifiers; }
        @Override public String getName() {return name;}
    }

    public static class TClassTree extends TDefinitionTree implements ClassTree {
        public final String superName;
        public final ConstructorTree constructor;
        public final List<? extends DefinitionTree> members;
        public TClassTree(Location location, String name, ModifiersTree modifiers,
                          String superName, ConstructorTree constructor,
                          List<? extends DefinitionTree> members) {
            super(location, name, modifiers);
            this.superName = superName;
            this.constructor = constructor;
            this.members = members;
        }
        @Override public String getSuperName() { return superName; }
        @Override public ConstructorTree getConstructor() { return constructor; }
        @Override public List<? extends DefinitionTree> getMembers() { return members; }
    }

    private static abstract class TNameInitTree extends TTree {
        public final String name;
        public final ExpressionTree initializer;
        private TNameInitTree(Location location, String name, ExpressionTree initializer) {
            super(location);
            this.name = name;
            this.initializer = initializer;
        }
        public String getName() { return name; }
        public ExpressionTree getInitializer() { return initializer; }
    }

    public static class TClosureTree extends TNameInitTree implements ClosureTree {
        public TClosureTree(Location location, String name, ExpressionTree initializer) {
            super(location, name, initializer);
        }
    }

    public static class TConstructorTree extends TTree implements ConstructorTree {
        public final ModifiersTree modifiers;
        public final List<? extends ParameterTree> parameters;
        public final List<? extends ArgumentTree> superArgs;
        public final BlockTree body;
        public TConstructorTree(Location location, ModifiersTree modifiers,
                                List<? extends ParameterTree> parameters,
                                List<? extends ArgumentTree> superArgs, BlockTree body) {
            super(location);
            this.modifiers = modifiers;
            this.parameters = parameters;
            this.superArgs = superArgs;
            this.body = body;
        }
        @Override public ModifiersTree getModifiers() { return modifiers; }
        @Override public List<? extends ParameterTree> getParameters() { return parameters; }
        @Override public List<? extends ArgumentTree> getSuperArguments() { return superArgs; }
        @Override public BlockTree getBody() { return body; }
    }

    public static class TContainerAccessTree extends TTree implements ContainerAccessTree {
        public final ExpressionTree container, key;
        public TContainerAccessTree(Location location, ExpressionTree container, ExpressionTree key) {
            super(location);
            this.container = container;
            this.key = key;
        }
        @Override public ExpressionTree getContainer() { return container; }
        @Override public ExpressionTree getKey() { return key; }
    }

    public static class TContinueTree extends TTree implements ContinueTree {
        public TContinueTree(Location location) {
            super(location);
        }
    }

    public static class TDictionaryTree extends TTree implements DictionaryTree {
        public final List<? extends ExpressionTree> key, values;
        public TDictionaryTree(Location location, List<? extends ExpressionTree> key,
                               List<? extends ExpressionTree> values) {
            super(location);
            this.key = key;
            this.values = values;
        }
        @Override public List<? extends ExpressionTree> getKeys() { return key; }
        @Override public List<? extends ExpressionTree> getValues() { return values; }
    }

    public static class TDoWhileTree extends TTree implements DoWhileTree {
        public final StatementTree statement;
        public final ExpressionTree condition;
        public TDoWhileTree(Location location, StatementTree statement, ExpressionTree condition) {
            super(location);
            this.statement = statement;
            this.condition = condition;
        }
        @Override public StatementTree getStatement() { return statement; }
        @Override public ExpressionTree getCondition() { return condition; }
    }

    public static class TExpressionStatementTree extends TTree implements ExpressionStatementTree {
        public final ExpressionTree expression;
        public TExpressionStatementTree(Location location, ExpressionTree expression) {
            super(location);
            this.expression = expression;
        }
        @Override public ExpressionTree getExpression() { return expression; }
    }

    public static class TFloatTree extends TLiteralTree<Double> implements FloatTree {
        public TFloatTree(Location location, Double value) {
            super(location, value);
        }
    }

    public static class TForLoopTree extends TTree implements ForLoopTree {
        public final VarDefTree runVar;
        public final ExpressionTree iterable;
        public final StatementTree statement;
        public TForLoopTree(Location location, VarDefTree runVar,
                            ExpressionTree iterable, StatementTree statement) {
            super(location);
            this.runVar = runVar;
            this.iterable = iterable;
            this.statement = statement;
        }
        @Override public VarDefTree getVariable() { return runVar; }
        @Override public ExpressionTree getIterable() { return iterable; }
        @Override public StatementTree getStatement() { return statement; }
    }

    public static class TFromImportTree extends TTree implements FromImportTree {
        public final List<String> fromChain, importChain;
        public TFromImportTree(Location location, List<String> fromChain, List<String> importChain) {
            super(location);
            this.fromChain = fromChain;
            this.importChain = importChain;
        }
        @Override public List<String> getFromAccessChain() { return fromChain; }
        @Override public List<String> getImportAccessChain() { return importChain; }
    }

    public static class TFunctionTree extends TDefinitionTree implements FunctionTree {
        public final List<? extends ParameterTree> parameters;
        public final BlockTree body;
        public TFunctionTree(Location location, String name, ModifiersTree modifiers,
                             List<? extends ParameterTree> parameters, BlockTree body) {
            super(location, name, modifiers);
            this.parameters = parameters;
            this.body = body;
        }
        @Override public List<? extends ParameterTree> getParameters() { return parameters; }
        @Override public BlockTree getBody() { return body; }
    }

    public static abstract class TUnaryTree extends TTree implements UnaryExpressionTree {
        public final ExpressionTree operand;
        private TUnaryTree(Location location, ExpressionTree operand) {
            super(location);
            this.operand = operand;
        }
        @Override public ExpressionTree getOperand() { return operand; }
    }

    public static class TGetTypeTree extends TUnaryTree implements GetTypeTree {
        public TGetTypeTree(Location location, ExpressionTree operand) {
            super(location, operand);
        }
    }

    public static class TIfElseTree extends TTree implements IfElseTree {
        public final ExpressionTree condition;
        public final StatementTree thenStatement;
        public final StatementTree elseStatement;
        public TIfElseTree(Location location, ExpressionTree condition,
                           StatementTree thenStatement, StatementTree elseStatement) {
            super(location);
            this.condition = condition;
            this.thenStatement = thenStatement;
            this.elseStatement = elseStatement;
        }
        @Override public ExpressionTree getCondition() { return condition; }
        @Override public StatementTree getThenStatement() { return thenStatement; }
        @Override public StatementTree getElseStatement() { return elseStatement; }
    }

    public static class TImportTree extends TTree implements ImportTree {
        public final List<String> accessChain;
        public TImportTree(Location location, List<String> accessChain) {
            super(location);
            this.accessChain = accessChain;
        }
        @Override public List<String> getAccessChain() { return accessChain; }
    }

    public static class TIntegerTree extends TLiteralTree<Integer> implements IntegerTree {
        public TIntegerTree(Location location, Integer value) {
            super(location, value);
        }
    }

    public static class TLambdaTree extends TTree implements LambdaTree {
        public final List<? extends ClosureTree> closures;
        public final List<? extends ParameterTree> parameters;
        public final BlockTree body;
        public TLambdaTree(Location location, List<? extends ClosureTree> closures,
                           List<? extends ParameterTree> parameters, BlockTree body) {
            super(location);
            this.closures = closures;
            this.parameters = parameters;
            this.body = body;
        }
        @Override public List<? extends ClosureTree> getClosures() { return closures; }
        @Override public List<? extends ParameterTree> getParameters() { return parameters; }
        @Override public BlockTree getBody() { return body; }
    }

    public static class TMemberAccessTree extends TTree implements MemberAccessTree {
        public final String memberName;
        public final ExpressionTree expression;
        public TMemberAccessTree(Location location, ExpressionTree expression, String memberName) {
            super(location);
            this.memberName = memberName;
            this.expression = expression;
        }
        @Override public ExpressionTree getExpression() { return expression; }
        @Override public String getMemberName() { return memberName; }
    }

    public static class TModifiersTree extends TTree implements ModifiersTree {
        public final Set<Modifier> modifiers;
        public TModifiersTree(Location location, Set<Modifier> modifiers) {
            super(location);
            this.modifiers = modifiers;
        }
        @Override public Set<Modifier> getModifiers() { return modifiers; }
    }

    public static class TNamespaceTree extends TDefinitionTree implements NamespaceTree {
        public final List<? extends DefinitionTree> definitions;
        public final List<? extends StatementTree> statements;
        public TNamespaceTree(Location location, String name, ModifiersTree modifiers,
                              List<? extends DefinitionTree> definitions,
                              List<? extends StatementTree> statements) {
            super(location, name, modifiers);
            this.definitions = definitions;
            this.statements = statements;
        }
        @Override public List<? extends DefinitionTree> getDefinitions() { return definitions; }
        @Override public List<? extends StatementTree> getStatements() { return statements; }
    }

    public static class TNotTree extends TUnaryTree implements NotTree {
        public TNotTree(Location location, ExpressionTree operand) {
            super(location, operand);
        }
    }

    public static class TNullTree extends TLiteralTree<Void> implements NullTree {
        public TNullTree(Location location) {
            super(location, null);
        }
    }

    public static class TParameterTree extends TDefinitionTree implements ParameterTree {
        public final ExpressionTree defaultValue;
        public TParameterTree(Location location, String name, ModifiersTree modifiers,
                              ExpressionTree defaultValue) {
            super(location, name, modifiers);
            this.defaultValue = defaultValue;
        }
        @Override public ExpressionTree getDefaultValue() { return defaultValue; }
    }

    public static class TRangeTree extends TTree implements RangeTree {
        public final ExpressionTree from, to;
        public TRangeTree(Location location, ExpressionTree from, ExpressionTree to) {
            super(location);
            this.from = from;
            this.to = to;
        }
        @Override public ExpressionTree getFrom() { return from; }
        @Override public ExpressionTree getTo() { return to; }
    }

    public static class TReturnTree extends TTree implements ReturnTree {
        public final ExpressionTree returned;
        public TReturnTree(Location location, ExpressionTree returned) {
            super(location);
            this.returned = returned;
        }
        @Override public ExpressionTree getExpression() { return returned; }
    }

    public static class TRootTree extends TTree implements RootTree {
        public final List<? extends DefinitionTree> definitions;
        public final List<? extends StatementTree> statements;

        public TRootTree(Location location, List<? extends DefinitionTree> definitions,
                         List<? extends StatementTree> statements) {
            super(location);
            this.definitions = definitions;
            this.statements = statements;
        }
        @Override public List<? extends DefinitionTree> getDefinitions() { return definitions; }
        @Override public List<? extends StatementTree> getStatements() { return statements; }
    }

    public static class TSignTree extends TUnaryTree implements SignTree {
        public final boolean isNegation;
        public TSignTree(Location location, boolean isNegation, ExpressionTree operand) {
            super(location, operand);
            this.isNegation = isNegation;
        }
        @Override public boolean isNegation() { return isNegation; }
    }

    public static class TStringTree extends TLiteralTree<String> implements StringTree {
        public TStringTree(Location location, String value) {
            super(location, value);
        }
    }

    public static class TThisTree extends TTree implements ThisTree {
        public TThisTree(Location location) {
            super(location);
        }
    }

    public static class TThrowsTree extends TTree implements ThrowTree {
        public final ExpressionTree thrown;
        public TThrowsTree(Location location, ExpressionTree thrown) {
            super(location);
            this.thrown = thrown;
        }
        @Override public ExpressionTree getThrown() { return thrown; }
    }

    public static class TTryCatchTree extends TTree implements TryCatchTree {
        public final StatementTree tryStatement, catchStatement;
        public final VarDefTree exceptionVar;
        public TTryCatchTree(Location location, StatementTree tryStatement, VarDefTree exceptionVar,
                             StatementTree catchStatement) {
            super(location);
            this.tryStatement = tryStatement;
            this.catchStatement = catchStatement;
            this.exceptionVar = exceptionVar;
        }
        @Override public StatementTree getTryStatement() { return tryStatement; }
        @Override public VarDefTree getExceptionVariable() { return exceptionVar; }
        @Override public StatementTree getCatchStatement() { return catchStatement; }
    }

    public static class TUseTree extends TTree implements UseTree {
        public final VariableTree varTree;
        public TUseTree(Location location, VariableTree varTree) {
            super(location);
            this.varTree = varTree;
        }
        @Override public VariableTree getVariable() { return varTree; }
    }

    public static class TVarDefsTree extends TTree implements VarDefsTree {
        public final ModifiersTree modifiers;
        public final List<? extends VarDefTree> definitions;
        public TVarDefsTree(Location location, ModifiersTree modifiers, List<? extends VarDefTree> definitions) {
            super(location);
            this.modifiers = modifiers;
            this.definitions = definitions;
        }
        @Override public ModifiersTree getModifiers() { return modifiers; }
        @Override public List<? extends VarDefTree> getDefinitions() { return definitions; }
    }

    public static class TVarDefTree extends TNameInitTree implements VarDefTree {
        public TVarDefTree(Location location, String name, ExpressionTree initializer) {
            super(location, name, initializer);
        }
    }

    public static class TVariableTree extends TTree implements VariableTree {
        public final String name;
        public TVariableTree(Location location, String name) {
            super(location);
            this.name = name;
        }
        @Override public String getName() { return name; }
    }

    public static class TWhileDoTree extends TTree implements WhileDoTree {
        public final ExpressionTree condition;
        public final StatementTree statement;
        public TWhileDoTree(Location location, ExpressionTree condition, StatementTree statement) {
            super(location);
            this.condition = condition;
            this.statement = statement;
        }
        @Override public ExpressionTree getCondition() { return condition; }
        @Override public StatementTree getStatement() { return statement; }
    }

}
