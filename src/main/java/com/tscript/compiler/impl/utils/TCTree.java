package com.tscript.compiler.impl.utils;

import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.impl.utils.Symbol.*;
import com.tscript.compiler.source.utils.Location;
import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;
import java.util.Set;

/**
 * A class for Tscript Compiler Tee implementation
 * @since 1.0
 * @author Lennart Köhler
 */
public abstract class TCTree implements Tree {

    private final Location location;

    private TCTree(Location location){
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public <P, R> R accept(Visitor<P, R> visitor){
        return accept(visitor, null);
    }

    public abstract <P, R> R accept(Visitor<P, R> visitor, P param);


    public static abstract class TCExpressionTree extends TCTree implements ExpressionTree {
        private TCExpressionTree(Location location) {
            super(location);
        }
    }

    public static abstract class TCStatementTree extends TCTree implements StatementTree {
        private TCStatementTree(Location location) {
            super(location);
        }
    }


    public static class TCArgumentTree extends TCTree implements ArgumentTree {

        public final String name;

        public final TCExpressionTree expression;

        public TCArgumentTree(Location location, String name, TCExpressionTree expression) {
            super(location);
            this.name = name;
            this.expression = expression;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ExpressionTree getExpression() {
            return expression;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitArgument(this, param);
        }
    }

    public static class TCArrayTree extends TCExpressionTree implements ArrayTree {

        public final List<? extends TCExpressionTree> content;

        public TCArrayTree(Location location, List<? extends TCExpressionTree> content) {
            super(location);
            this.content = content;
        }

        @Override
        public List<? extends ExpressionTree> getContents() {
            return content;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitArray(this, param);
        }
    }

    private abstract static class TCBinaryTree extends TCExpressionTree implements BinaryExpressionTree {

        public final TCExpressionTree left;

        public final TCExpressionTree right;

        public TCBinaryTree(Location location, TCExpressionTree left, TCExpressionTree right) {
            super(location);
            this.left = left;
            this.right = right;
        }
        @Override
        public ExpressionTree getLeftOperand() {
            return left;
        }

        @Override
        public ExpressionTree getRightOperand() {
            return right;
        }
    }

    public static class TCAssignTree extends TCBinaryTree implements AssignTree {

        public TCAssignTree(Location location, TCExpressionTree left, TCExpressionTree right) {
            super(location, left, right);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitAssign(this, param);
        }
    }

    public static class TCBinaryOperationTree extends TCBinaryTree implements BinaryOperationTree {

        public final Operation operation;

        public TCBinaryOperationTree(Location location, TCExpressionTree left, TCExpressionTree right, Operation operation) {
            super(location, left, right);
            this.operation = operation;
        }

        @Override
        public Operation getOperationType() {
            return operation;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitBinaryOperation(this, param);
        }
    }

    public static class TCBlockTree extends TCStatementTree implements BlockTree {

        public final List<? extends TCStatementTree> statements;

        public TCBlockTree(Location location, List<? extends TCStatementTree> statements) {
            super(location);
            this.statements = statements;
        }

        @Override
        public List<? extends StatementTree> getStatements() {
            return statements;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitBlock(this, param);
        }
    }

    private abstract static class TCLiteralTree<T> extends TCExpressionTree implements LiteralTree<T> {

        public final T value;

        private TCLiteralTree(Location location, T value) {
            super(location);
            this.value = value;
        }
        @Override
        public T get() {
            return value;
        }
    }

    public static class TCBooleanTree extends TCLiteralTree<Boolean> implements BooleanTree {
        public TCBooleanTree(Location location, Boolean value) {
            super(location, value);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitBoolean(this, param);
        }
    }

    public static class TCBreakTree extends TCStatementTree implements BreakTree {
        public TCBreakTree(Location location) {
            super(location);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitBreak(this, param);
        }
    }

    public static class TCCallTree extends TCExpressionTree implements CallTree {

        public final TCExpressionTree called;

        public final List<? extends TCArgumentTree> arguments;

        public TCCallTree(Location location, TCExpressionTree called, List<? extends TCArgumentTree> arguments) {
            super(location);
            this.called = called;
            this.arguments = arguments;
        }

        @Override
        public ExpressionTree getCalled() {
            return called;
        }

        @Override
        public List<? extends ArgumentTree> getArguments() {
            return arguments;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitCall(this, param);
        }
    }

    public static abstract class TCDefinitionTree extends TCStatementTree implements DefinitionTree {

        public final String name;

        public final TCModifiersTree modifiers;

        private TCDefinitionTree(Location location, String name, TCModifiersTree modifiers) {
            super(location);
            this.name = name;
            this.modifiers = modifiers;
        }

        @Override public ModifiersTree getModifiers() {
            return modifiers;
        }

        @Override public String getName() {
            return name;
        }
    }

    public static class TCClassTree extends TCDefinitionTree implements ClassTree {

        public final String superName;

        public final List<? extends TCTree> members;

        public ClassSymbol sym;

        public TCClassTree(Location location, String name, TCModifiersTree modifiers,
                           String superName, List<? extends TCTree> members) {
            super(location, name, modifiers);
            this.superName = superName;
            this.members = members;
        }

        @Override
        public String getSuperName() {
            return superName;
        }

        @Override
        public List<? extends Tree> getMembers() {
            return members;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitClass(this, param);
        }
    }

    private static abstract class TCNameInitTree extends TCTree {

        public final String name;

        public final TCExpressionTree initializer;

        private TCNameInitTree(Location location, String name, TCExpressionTree initializer) {
            super(location);
            this.name = name;
            this.initializer = initializer;
        }

        public String getName() {
            return name;
        }
        public ExpressionTree getInitializer() {
            return initializer;
        }
    }

    public static class TCClosureTree extends TCNameInitTree implements ClosureTree {
        public TCClosureTree(Location location, String name, TCExpressionTree initializer) {
            super(location, name, initializer);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitClosure(this, param);
        }
    }

    public static class TCConstructorTree extends TCTree implements ConstructorTree {

        public final TCModifiersTree modifiers;

        public final List<? extends TCParameterTree> parameters;

        public final List<? extends TCArgumentTree> superArgs;

        public final TCBlockTree body;

        public TCConstructorTree(Location location, TCModifiersTree modifiers,
                                 List<? extends TCParameterTree> parameters,
                                 List<? extends TCArgumentTree> superArgs, TCBlockTree body) {
            super(location);
            this.modifiers = modifiers;
            this.parameters = parameters;
            this.superArgs = superArgs;
            this.body = body;
        }

        @Override
        public ModifiersTree getModifiers() {
            return modifiers;
        }

        @Override
        public List<? extends ParameterTree> getParameters() {
            return parameters;
        }

        @Override
        public List<? extends ArgumentTree> getSuperArguments() {
            return superArgs;
        }

        @Override
        public BlockTree getBody() {
            return body;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitConstructor(this, param);
        }
    }

    public static class TCContainerAccessTree extends TCExpressionTree implements ContainerAccessTree {

        public final TCExpressionTree container;

        public final TCExpressionTree key;

        public TCContainerAccessTree(Location location, TCExpressionTree container, TCExpressionTree key) {
            super(location);
            this.container = container;
            this.key = key;
        }

        @Override
        public ExpressionTree getContainer() {
            return container;
        }

        @Override
        public ExpressionTree getKey() {
            return key;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitContainerAccess(this, param);
        }
    }

    public static class TCContinueTree extends TCStatementTree implements ContinueTree {
        public TCContinueTree(Location location) {
            super(location);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitContinue(this, param);
        }
    }

    public static class TCDictionaryTree extends TCExpressionTree implements DictionaryTree {

        public final List<? extends TCExpressionTree> keys;

        public final List<? extends TCExpressionTree> values;

        public TCDictionaryTree(Location location, List<? extends TCExpressionTree> key,
                                List<? extends TCExpressionTree> values) {
            super(location);
            this.keys = key;
            this.values = values;
        }

        @Override
        public List<? extends ExpressionTree> getKeys() {
            return keys;
        }

        @Override
        public List<? extends ExpressionTree> getValues() {
            return values;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitDictionary(this, param);
        }
    }

    public static class TCDoWhileTree extends TCStatementTree implements DoWhileTree {

        public final TCStatementTree statement;

        public final TCExpressionTree condition;

        public TCDoWhileTree(Location location, TCStatementTree statement, TCExpressionTree condition) {
            super(location);
            this.statement = statement;
            this.condition = condition;
        }

        @Override
        public StatementTree getStatement() {
            return statement;
        }
        @Override
        public ExpressionTree getCondition() {
            return condition;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitDoWhileLoop(this, param);
        }
    }

    public static class TCExpressionStatementTree extends TCStatementTree implements ExpressionStatementTree {

        public final TCExpressionTree expression;

        public TCExpressionStatementTree(Location location, TCExpressionTree expression) {
            super(location);
            this.expression = expression;
        }

        @Override
        public ExpressionTree getExpression() {
            return expression;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitExpressionStatement(this, param);
        }
    }

    public static class TCFloatTree extends TCLiteralTree<Double> implements FloatTree {
        public TCFloatTree(Location location, Double value) {
            super(location, value);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitFloat(this, param);
        }
    }

    public static class TCForLoopTree extends TCStatementTree implements ForLoopTree {

        public final TCVarDefTree runVar;

        public final TCExpressionTree iterable;

        public final TCStatementTree statement;

        public TCForLoopTree(Location location, TCVarDefTree runVar,
                             TCExpressionTree iterable, TCStatementTree statement) {
            super(location);
            this.runVar = runVar;
            this.iterable = iterable;
            this.statement = statement;
        }

        @Override
        public VarDefTree getVariable() {
            return runVar;
        }

        @Override
        public ExpressionTree getIterable() {
            return iterable;
        }

        @Override
        public StatementTree getStatement() {
            return statement;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitForLoop(this, param);
        }
    }

    public static class TCFromImportTree extends TCStatementTree implements FromImportTree {

        public final List<String> fromChain;

        public final List<String> importChain;

        public TCFromImportTree(Location location, List<String> fromChain, List<String> importChain) {
            super(location);
            this.fromChain = fromChain;
            this.importChain = importChain;
        }

        @Override
        public List<String> getFromAccessChain() {
            return fromChain;
        }

        @Override
        public List<String> getImportAccessChain() {
            return importChain;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitFromImport(this, param);
        }
    }

    public static class TCFunctionTree extends TCDefinitionTree implements FunctionTree {

        public final List<? extends TCParameterTree> parameters;

        public final TCBlockTree body;

        public FunctionSymbol sym;

        public TCFunctionTree(Location location, String name, TCModifiersTree modifiers,
                              List<? extends TCParameterTree> parameters, TCBlockTree body) {
            super(location, name, modifiers);
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        public List<? extends ParameterTree> getParameters() {
            return parameters;
        }

        @Override
        public BlockTree getBody() {
            return body;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitFunction(this, param);
        }
    }

    public static abstract class TCUnaryTree extends TCExpressionTree implements UnaryExpressionTree {

        public final TCExpressionTree operand;

        private TCUnaryTree(Location location, TCExpressionTree operand) {
            super(location);
            this.operand = operand;
        }

        @Override
        public ExpressionTree getOperand() {
            return operand;
        }
    }

    public static class TCGetTypeTree extends TCUnaryTree implements GetTypeTree {
        public TCGetTypeTree(Location location, TCExpressionTree operand) {
            super(location, operand);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitGetType(this, param);
        }
    }

    public static class TCIfElseTree extends TCStatementTree implements IfElseTree {

        public final TCExpressionTree condition;

        public final TCStatementTree thenStatement;

        public final TCStatementTree elseStatement;

        public TCIfElseTree(Location location, TCExpressionTree condition,
                            TCStatementTree thenStatement, TCStatementTree elseStatement) {
            super(location);
            this.condition = condition;
            this.thenStatement = thenStatement;
            this.elseStatement = elseStatement;
        }

        @Override
        public ExpressionTree getCondition() {
            return condition;
        }

        @Override
        public StatementTree getThenStatement() {
            return thenStatement;
        }

        @Override
        public StatementTree getElseStatement() {
            return elseStatement;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitIfElse(this, param);
        }
    }

    public static class TCImportTree extends TCStatementTree implements ImportTree {

        public final List<String> accessChain;

        public TCImportTree(Location location, List<String> accessChain) {
            super(location);
            this.accessChain = accessChain;
        }

        @Override
        public List<String> getAccessChain() {
            return accessChain;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitImport(this, param);
        }
    }

    public static class TCIntegerTree extends TCLiteralTree<Integer> implements IntegerTree {
        public TCIntegerTree(Location location, Integer value) {
            super(location, value);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitInteger(this, param);
        }
    }

    public static class TCLambdaTree extends TCExpressionTree implements LambdaTree {

        public final List<? extends TCClosureTree> closures;

        public final List<? extends TCParameterTree> parameters;

        public final TCBlockTree body;

        public TCLambdaTree(Location location, List<? extends TCClosureTree> closures,
                            List<? extends TCParameterTree> parameters, TCBlockTree body) {
            super(location);
            this.closures = closures;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        public List<? extends ClosureTree> getClosures() {
            return closures;
        }

        @Override
        public List<? extends ParameterTree> getParameters() {
            return parameters;
        }

        @Override
        public BlockTree getBody() {
            return body;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitLambda(this, param);
        }
    }

    public static class TCMemberAccessTree extends TCExpressionTree implements MemberAccessTree {

        public final String memberName;

        public final TCExpressionTree expression;

        public TCMemberAccessTree(Location location, TCExpressionTree expression, String memberName) {
            super(location);
            this.memberName = memberName;
            this.expression = expression;
        }

        @Override
        public ExpressionTree getExpression() {
            return expression;
        }

        @Override
        public String getMemberName() {
            return memberName;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitMemberAccess(this, param);
        }
    }

    public static class TCModifiersTree extends TCTree implements ModifiersTree {

        public final Set<Modifier> modifiers;

        public TCModifiersTree(Location location, Set<Modifier> modifiers) {
            super(location);
            this.modifiers = modifiers;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return modifiers;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitModifiers(this, param);
        }
    }

    public static class TCNamespaceTree extends TCDefinitionTree implements NamespaceTree {

        public final List<? extends TCDefinitionTree> definitions;

        public final List<? extends TCStatementTree> statements;

        public NamespaceSymbol sym;

        public TCNamespaceTree(Location location, String name, TCModifiersTree modifiers,
                               List<? extends TCDefinitionTree> definitions,
                               List<? extends TCStatementTree> statements) {
            super(location, name, modifiers);
            this.definitions = definitions;
            this.statements = statements;
        }

        @Override
        public List<? extends DefinitionTree> getDefinitions() {
            return definitions;
        }

        @Override
        public List<? extends StatementTree> getStatements() {
            return statements;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitNamespace(this, param);
        }
    }

    public static class TCNotTree extends TCUnaryTree implements NotTree {
        public TCNotTree(Location location, TCExpressionTree operand) {
            super(location, operand);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitNot(this, param);
        }
    }

    public static class TCNullTree extends TCLiteralTree<Void> implements NullTree {
        public TCNullTree(Location location) {
            super(location, null);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitNull(this, param);
        }
    }

    public static class TCParameterTree extends TCDefinitionTree implements ParameterTree {

        public final TCExpressionTree defaultValue;

        public TCParameterTree(Location location, String name, TCModifiersTree modifiers,
                               TCExpressionTree defaultValue) {
            super(location, name, modifiers);
            this.defaultValue = defaultValue;
        }

        @Override
        public ExpressionTree getDefaultValue() {
            return defaultValue;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitParameter(this, param);
        }
    }

    public static class TCRangeTree extends TCExpressionTree implements RangeTree {

        public final TCExpressionTree from;

        public TCExpressionTree to;

        public TCRangeTree(Location location, TCExpressionTree from, TCExpressionTree to) {
            super(location);
            this.from = from;
            this.to = to;
        }

        @Override
        public ExpressionTree getFrom() {
            return from;
        }

        @Override
        public ExpressionTree getTo() {
            return to;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitRange(this, param);
        }
    }

    public static class TCReturnTree extends TCStatementTree implements ReturnTree {

        public final TCExpressionTree returned;

        public TCReturnTree(Location location, TCExpressionTree returned) {
            super(location);
            this.returned = returned;
        }

        @Override
        public ExpressionTree getExpression() {
            return returned;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitReturn(this, param);
        }
    }

    public static class TCRootTree extends TCTree implements RootTree {

        public final List<? extends TCDefinitionTree> definitions;

        public final List<? extends TCStatementTree> statements;

        public TCRootTree(Location location, List<? extends TCDefinitionTree> definitions,
                          List<? extends TCStatementTree> statements) {
            super(location);
            this.definitions = definitions;
            this.statements = statements;
        }

        @Override
        public List<? extends DefinitionTree> getDefinitions() {
            return definitions;
        }

        @Override
        public List<? extends StatementTree> getStatements() {
            return statements;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitRoot(this, param);
        }
    }

    public static class TCSignTree extends TCUnaryTree implements SignTree {

        public final boolean isNegation;

        public TCSignTree(Location location, boolean isNegation, TCExpressionTree operand) {
            super(location, operand);
            this.isNegation = isNegation;
        }

        @Override
        public boolean isNegation() {
            return isNegation;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitSign(this, param);
        }
    }

    public static class TCStringTree extends TCLiteralTree<String> implements StringTree {
        public TCStringTree(Location location, String value) {
            super(location, value);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitString(this, param);
        }
    }

    public static class TCSuperTree extends TCExpressionTree implements SuperTree {

        public final String name;

        public TCSuperTree(Location location, String name) {
            super(location);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitSuper(this, param);
        }
    }

    public static class TCThisTree extends TCExpressionTree implements ThisTree {
        public TCThisTree(Location location) {
            super(location);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitThis(this, param);
        }
    }

    public static class TCThrowTree extends TCStatementTree implements ThrowTree {

        public final TCExpressionTree thrown;

        public TCThrowTree(Location location, TCExpressionTree thrown) {
            super(location);
            this.thrown = thrown;
        }

        @Override
        public ExpressionTree getThrown() {
            return thrown;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitThrow(this, param);
        }
    }

    public static class TCTryCatchTree extends TCStatementTree implements TryCatchTree {

        public final TCStatementTree tryStatement;

        public final TCVarDefTree exceptionVar;

        public final TCStatementTree catchStatement;

        public TCTryCatchTree(Location location, TCStatementTree tryStatement, TCVarDefTree exceptionVar,
                              TCStatementTree catchStatement) {
            super(location);
            this.tryStatement = tryStatement;
            this.catchStatement = catchStatement;
            this.exceptionVar = exceptionVar;
        }

        @Override
        public StatementTree getTryStatement() {
            return tryStatement;
        }

        @Override
        public VarDefTree getExceptionVariable() {
            return exceptionVar;
        }

        @Override
        public StatementTree getCatchStatement() {
            return catchStatement;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitTryCatch(this, param);
        }
    }

    public static class TCUseTree extends TCTree implements UseTree {

        public final TCVariableTree varTree;

        public TCUseTree(Location location, TCVariableTree varTree) {
            super(location);
            this.varTree = varTree;
        }

        @Override
        public VariableTree getVariable() {
            return varTree;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitUse(this, param);
        }
    }

    public static class TCVarDefsTree extends TCStatementTree implements VarDefsTree {

        public final TCModifiersTree modifiers;

        public final List<? extends TCVarDefTree> definitions;

        public TCVarDefsTree(Location location, TCModifiersTree modifiers, List<? extends TCVarDefTree> definitions) {
            super(location);
            this.modifiers = modifiers;
            this.definitions = definitions;
        }

        @Override
        public ModifiersTree getModifiers() {
            return modifiers;
        }

        @Override
        public List<? extends VarDefTree> getDefinitions() {
            return definitions;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitVarDefs(this, param);
        }
    }

    public static class TCVarDefTree extends TCNameInitTree implements VarDefTree {

        public VarSymbol sym;

        public TCVarDefTree(Location location, String name, TCExpressionTree initializer) {
            super(location, name, initializer);
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitVarDef(this, param);
        }
    }

    public static class TCVariableTree extends TCExpressionTree implements VariableTree {

        public final String name;

        Symbol sym;

        public TCVariableTree(Location location, String name) {
            super(location);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitVariable(this, param);
        }
    }

    public static class TCWhileDoTree extends TCStatementTree implements WhileDoTree {

        public final TCExpressionTree condition;

        public final TCStatementTree statement;

        public TCWhileDoTree(Location location, TCExpressionTree condition, TCStatementTree statement) {
            super(location);
            this.condition = condition;
            this.statement = statement;
        }

        @Override
        public ExpressionTree getCondition() {
            return condition;
        }

        @Override
        public StatementTree getStatement() {
            return statement;
        }

        @Override
        public <P, R> R accept(Visitor<P, R> visitor, P param) {
            return visitor.visitWhileDoLoop(this, param);
        }
    }


    public interface Factory {

        TCArgumentTree ArgumentTree(Location location,
                                  String name,
                                  TCExpressionTree value);

        TCArrayTree ArrayTree(Location location,
                            List<? extends TCExpressionTree> content);

        TCAssignTree AssignTree(Location location,
                              TCExpressionTree assigned,
                              TCExpressionTree value);

        TCBinaryOperationTree BinaryOperationTree(Location location,
                                                TCExpressionTree left,
                                                TCExpressionTree right,
                                                Operation operation);

        TCBlockTree BlockTree(Location location,
                            List<? extends TCStatementTree> statements);

        TCBooleanTree BooleanTree(Location location,
                                boolean value);

        TCBreakTree BreakTree(Location location);

        TCCallTree CallTree(Location location,
                          TCExpressionTree called,
                          List<? extends TCArgumentTree> arguments);

        TCClassTree ClassTree(Location location,
                            TCModifiersTree modifiers,
                            String name,
                            String superName,
                            List<? extends TCTree> members);

        TCClosureTree ClosureTree(Location location,
                                String name,
                                TCExpressionTree initializer);

        TCConstructorTree ConstructorTree(Location location,
                                        TCModifiersTree modifiers,
                                        List<? extends TCParameterTree> parameters,
                                        List<? extends TCArgumentTree> superArguments,
                                        TCBlockTree body);

        TCContainerAccessTree ContainerAccessTree(Location location,
                                                TCExpressionTree container,
                                                TCExpressionTree key);

        TCContinueTree ContinueTree(Location location);

        TCDictionaryTree DictionaryTree(Location location,
                                      List<? extends TCExpressionTree> keys,
                                      List<? extends TCExpressionTree> values);

        TCDoWhileTree DoWhileTree(Location location,
                                TCStatementTree statement,
                                TCExpressionTree condition);

        TCExpressionStatementTree ExpressionStatementTree(Location location,
                                                        TCExpressionTree expression);

        TCFloatTree FloatTree(Location location,
                            double value);

        TCForLoopTree ForLoopTree(Location location,
                                TCVarDefTree runVar,
                                TCExpressionTree iterable,
                                TCStatementTree statement);

        TCFromImportTree FromImportTree(Location location,
                                      List<String> fromAccessChain,
                                      List<String> importAccessChain);

        TCFunctionTree FunctionTree(Location location,
                                  TCModifiersTree modifiers,
                                  String name,
                                  List<? extends TCParameterTree> parameters,
                                  TCBlockTree body);

        TCGetTypeTree GetTypeTree(Location location,
                                TCExpressionTree operand);

        TCIfElseTree IfElseTree(Location location,
                              TCExpressionTree condition,
                              TCStatementTree thenStatement,
                              TCStatementTree elseStatement);

        TCImportTree ImportTree(Location location,
                              List<String> importAccessChain);

        TCIntegerTree IntegerTree(Location location,
                                int value);

        TCLambdaTree LambdaTree(Location location,
                              List<? extends TCClosureTree> closures,
                              List<? extends TCParameterTree> parameters,
                              TCBlockTree body);

        TCMemberAccessTree MemberAccessTree(Location location,
                                          TCExpressionTree accessed,
                                          String memberName);

        TCModifiersTree ModifiersTree(Location location,
                                    Set<Modifier> modifiers);

        TCNamespaceTree NamespaceTree(Location location,
                                    TCModifiersTree modifiers,
                                    String name,
                                    List<? extends TCDefinitionTree> definitions,
                                    List<? extends TCStatementTree> statements);

        TCNotTree NotTree(Location location,
                        TCExpressionTree operand);

        TCNullTree NullTree(Location location);

        TCParameterTree ParameterTree(Location location,
                                    String name,
                                    TCModifiersTree modifiers,
                                    TCExpressionTree defaultValue);

        TCRangeTree RangeTree(Location location,
                            TCExpressionTree from,
                            TCExpressionTree to);

        TCReturnTree ReturnTree(Location location,
                              TCExpressionTree returned);

        TCRootTree RootTree(Location location,
                          List<? extends TCDefinitionTree> definitions,
                          List<? extends TCStatementTree> statements);

        TCSignTree SignTree(Location location,
                          boolean isNegation,
                          TCExpressionTree operand);

        TCStringTree StringTree(Location location,
                              String value);

        TCExpressionTree SuperTree(Location location, String name);

        TCThisTree ThisTree(Location location);

        TCThrowTree ThrowTree(Location location,
                            TCExpressionTree thrown);

        TCTryCatchTree TryCatchTree(Location location,
                                  TCStatementTree tryBody,
                                  TCVarDefTree exceptionVar,
                                  TCStatementTree catchBody);

        TCUseTree UseTree(Location location,
                        TCVariableTree variable);

        TCVarDefsTree VarDefsTree(Location location,
                                TCModifiersTree modifiers,
                                List<? extends TCVarDefTree> definedVars);

        TCVarDefTree VarDefTree(Location location,
                              String name,
                              TCExpressionTree initializer);

        TCVariableTree VariableTree(Location location,
                                  String name);

        TCWhileDoTree WhileDoTree(Location location,
                                TCExpressionTree condition,
                                TCStatementTree statement);

    }


    public interface Visitor<P, R> {
        R visitArgument(TCArgumentTree node, P p);
        R visitArray(TCArrayTree node, P p);
        R visitAssign(TCAssignTree node, P p);
        R visitBinaryOperation(TCBinaryOperationTree node, P p);
        R visitBlock(TCBlockTree node, P p);
        R visitBoolean(TCBooleanTree node, P p);
        R visitBreak(TCBreakTree node, P p);
        R visitCall(TCCallTree node, P p);
        R visitClass(TCClassTree node, P p);
        R visitClosure(TCClosureTree node, P p);
        R visitConstructor(TCConstructorTree node, P p);
        R visitContainerAccess(TCContainerAccessTree node, P p);
        R visitContinue(TCContinueTree node, P p);
        R visitDictionary(TCDictionaryTree node, P p);
        R visitDoWhileLoop(TCDoWhileTree node, P p);
        R visitExpressionStatement(TCExpressionStatementTree node, P p);
        R visitFloat(TCFloatTree node, P p);
        R visitForLoop(TCForLoopTree node, P p);
        R visitFromImport(TCFromImportTree node, P p);
        R visitFunction(TCFunctionTree node, P p);
        R visitGetType(TCGetTypeTree node, P p);
        R visitIfElse(TCIfElseTree node, P p);
        R visitImport(TCImportTree node, P p);
        R visitInteger(TCIntegerTree node, P p);
        R visitLambda(TCLambdaTree node, P p);
        R visitMemberAccess(TCMemberAccessTree node, P p);
        R visitModifiers(TCModifiersTree node, P p);
        R visitNamespace(TCNamespaceTree node, P p);
        R visitNot(TCNotTree node, P p);
        R visitNull(TCNullTree node, P p);
        R visitParameter(TCParameterTree node, P p);
        R visitRange(TCRangeTree node, P p);
        R visitReturn(TCReturnTree node, P p);
        R visitRoot(TCRootTree node, P p);
        R visitSign(TCSignTree node, P p);
        R visitString(TCStringTree node, P p);
        R visitSuper(TCSuperTree node, P p);
        R visitThis(TCThisTree node, P p);
        R visitThrow(TCThrowTree node, P p);
        R visitTryCatch(TCTryCatchTree node, P p);
        R visitUse(TCUseTree node, P p);
        R visitVarDefs(TCVarDefsTree node, P p);
        R visitVarDef(TCVarDefTree node, P p);
        R visitVariable(TCVariableTree node, P p);
        R visitWhileDoLoop(TCWhileDoTree node, P p);
    }

}
