package tscriptc.tree;

import tscriptc.util.Location;
import tscriptc.util.TreeVisitor;

import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unused")
public class Trees {

    private Trees(){}


    public static abstract class AbstractTree implements Tree {
        public Location location;
        public AbstractTree(Location location){
            this.location = location;
        }
        public Location getLocation() {
            return location;
        }
    }

    public static class BasicArgumentTree extends AbstractTree implements ArgumentTree {
        public ExpressionTree expression;
        public String referenceName;
        public BasicArgumentTree(Location location, ExpressionTree expression) {
            super(location);
            this.expression = expression;
        }
        public BasicArgumentTree(Location location, String referenceName, ExpressionTree expression) {
            super(location);
            this.referenceName = referenceName;
            this.expression = expression;
        }
        @Override
        public String getReferencedName() {
            return referenceName;
        }
        @Override
        public ExpressionTree getExpression() {
            return expression;
        }
    }


    public static class BasicArrayTree extends AbstractTree implements ArrayTree {
        public final List<ExpressionTree> content = new ArrayList<>();
        public BasicArrayTree(Location location) {
            super(location);
        }
        @Override
        public List<ExpressionTree> getContent() {
            return content;
        }
    }

    public static abstract class BasicBinaryExpressionTree extends AbstractTree implements BinaryExpressionTree {
        public ExpressionTree left, right;
        public BasicBinaryExpressionTree(Location location) {
            super(location);
        }
        public BasicBinaryExpressionTree(Location location, ExpressionTree left, ExpressionTree right) {
            super(location);
            this.left = left;
            this.right = right;
        }
        @Override
        public ExpressionTree getLeft() {
            return left;
        }
        @Override
        public ExpressionTree getRight() {
            return right;
        }
    }

    public static class BasicAssignTree extends BasicBinaryExpressionTree implements AssignTree {
        public BasicAssignTree(Location location) {
            super(location);
        }
        public BasicAssignTree(Location location, ExpressionTree left, ExpressionTree right) {
            super(location, left, right);
        }
    }

    public static class BasicBinaryOperationTree extends BasicBinaryExpressionTree implements BinaryOperationTree {
        public Operation operation;
        public BasicBinaryOperationTree(Location location) {
            super(location);
        }
        public BasicBinaryOperationTree(Location location, ExpressionTree left, ExpressionTree right, Operation operation) {
            super(location, left, right);
            this.operation = operation;
        }
        @Override
        public Operation getOperation() {
            return operation;
        }
    }

    public static class BasicBlockTree extends AbstractTree implements BlockTree {
        public final List<StatementTree> statements = new ArrayList<>();
        public BasicBlockTree() {
            super(null);
        }
        @Override
        public List<StatementTree> getStatements() {
            return statements;
        }
    }

    public static class BasicBooleanLiteralTree extends AbstractTree implements BooleanLiteralTree {
        public boolean b;
        public BasicBooleanLiteralTree(Location location, boolean b) {
            super(location);
            this.b = b;
        }
        @Override
        public Boolean get() {
            return b;
        }
    }

    public static class BasicBreakTree extends AbstractTree implements BreakTree {
        public BasicBreakTree(Location location) {
            super(location);
        }
    }

    public static abstract class BasicDefinitionTree extends AbstractTree implements DefinitionTree {
        public String name;
        public final Set<Modifier> modifiers = new HashSet<>();
        public BasicDefinitionTree(Location location, String name) {
            super(location);
            this.name = name;
        }
        public BasicDefinitionTree(Location location, String name, Collection<Modifier> modifiers) {
            super(location);
            this.name = name;
            this.modifiers.addAll(modifiers);
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public Set<Modifier> getModifiers() {
            return modifiers;
        }
    }

    public static abstract class BasicCallableTree extends BasicDefinitionTree implements CallableTree {
        public final List<ParameterTree> parameters = new ArrayList<>();
        public BasicCallableTree(Location location, String name) {
            super(location, name);
        }
        public BasicCallableTree(Location location, String name, Collection<Modifier> modifiers) {
            super(location, name, modifiers);
        }
        @Override
        public List<ParameterTree> getParameters() {
            return parameters;
        }
    }

    public static abstract class BasicUnaryExpressionTree extends AbstractTree implements UnaryExpressionTree {
        public ExpressionTree expression;
        public BasicUnaryExpressionTree(Location location) {
            super(location);
        }
        public BasicUnaryExpressionTree(Location location, ExpressionTree expression) {
            super(location);
            this.expression = expression;
        }
        @Override
        public ExpressionTree getExpression() {
            return expression;
        }
    }

    public static class BasicCallTree extends BasicUnaryExpressionTree implements CallTree {
        public final List<ArgumentTree> arguments = new ArrayList<>();
        public BasicCallTree(Location location) {
            super(location);
        }
        public BasicCallTree(Location location, ExpressionTree expression) {
            super(location, expression);
        }
        @Override
        public List<ArgumentTree> getArguments() {
            return arguments;
        }
    }

    public static class BasicClassTree extends BasicDefinitionTree implements ClassTree {
        public String superName;
        public ConstructorTree constructor;
        public final List<DefinitionTree> definitions = new ArrayList<>();
        public BasicClassTree(Location location, String name) {
            super(location, name);
        }
        public BasicClassTree(Location location, String name, Collection<Modifier> modifiers) {
            super(location, name, modifiers);
        }
        @Override
        public String getSuper() {
            return superName;
        }
        @Override
        public ConstructorTree getConstructor() {
            return constructor;
        }
        @Override
        public List<DefinitionTree> getDefinitions() {
            return definitions;
        }
    }

    public static class BasicClosureTree extends AbstractTree implements ClosureTree {
        public String name;
        public ExpressionTree expression;
        public BasicClosureTree(Location location, String name) {
            super(location);
        }
        public BasicClosureTree(Location location, String name, ExpressionTree expression) {
            super(location);
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
    }

    public static class BasicConstructorTree extends AbstractTree implements ConstructorTree {
        public final Set<Modifier> modifiers = new HashSet<>();
        public final List<ParameterTree> parameters = new ArrayList<>();
        public final List<ArgumentTree> superArguments = new ArrayList<>();
        public BlockTree body;
        public BasicConstructorTree(Location location) {
            super(location);
        }
        @Override
        public Set<Modifier> getModifiers() {
            return modifiers;
        }
        @Override
        public List<ParameterTree> getParameters() {
            return parameters;
        }
        @Override
        public List<ArgumentTree> getSuperArguments() {
            return superArguments;
        }
        @Override
        public BlockTree getBody() {
            return body;
        }
    }

    public static class BasicContainerAccessTree extends BasicUnaryExpressionTree implements ContainerAccessTree {
        public ExpressionTree key;
        public BasicContainerAccessTree(Location location) {
            super(location);
        }
        public BasicContainerAccessTree(Location location, ExpressionTree expression, ExpressionTree key) {
            super(location, expression);
            this.key = key;
        }
        @Override
        public ExpressionTree getKey() {
            return key;
        }
    }

    public static class BasicContinueTree extends AbstractTree implements ContinueTree {
        public BasicContinueTree(Location location) {
            super(location);
        }
    }

    public static class BasicDictionaryTree extends AbstractTree implements DictionaryTree {
        public final List<ExpressionTree> keys = new ArrayList<>();
        public final List<ExpressionTree> values = new ArrayList<>();
        public BasicDictionaryTree(Location location) {
            super(location);
        }
        @Override
        public List<ExpressionTree> getKeys() {
            return keys;
        }
        @Override
        public List<ExpressionTree> getValues() {
            return values;
        }
    }

    public static class BasicDoWhileTree extends AbstractTree implements DoWhileTree {
        public ExpressionTree condition;
        public StatementTree body;
        public BasicDoWhileTree(Location location) {
            super(location);
        }
        public BasicDoWhileTree(Location location, StatementTree body, ExpressionTree condition) {
            super(location);
            this.body = body;
            this.condition = condition;
        }
        @Override
        public StatementTree getBody() {
            return body;
        }
        @Override
        public ExpressionTree getCondition() {
            return condition;
        }
    }

    public static class BasicExpressionStatementTree extends AbstractTree implements ExpressionStatementTree {
        public ExpressionTree expression;
        public BasicExpressionStatementTree(ExpressionTree expression) {
            super(expression.getLocation());
            this.expression = expression;
        }
        @Override
        public ExpressionTree getExpression() {
            return expression;
        }
    }

    public static class BasicFloatLiteralTree extends AbstractTree implements FloatLiteralTree {
        public double f;
        public BasicFloatLiteralTree(Location location, double f) {
            super(location);
            this.f = f;
        }
        @Override
        public Double get() {
            return f;
        }
    }

    public static class BasicForLoopTree extends AbstractTree implements ForLoopTree {
        public String name;
        public boolean isDeclaration = false;
        public ExpressionTree iterable;
        public StatementTree body;
        public BasicForLoopTree(Location location, boolean isDeclaration, String name) {
            super(location);
            this.isDeclaration = isDeclaration;
            this.name = name;
        }
        public BasicForLoopTree(Location location, ExpressionTree iterable){
            super(location);
            this.iterable = iterable;
        }
        public BasicForLoopTree(Location location, boolean isDeclaration, String name, ExpressionTree iterable) {
            super(location);
            this.isDeclaration = isDeclaration;
            this.name = name;
            this.iterable = iterable;
        }

        @Override
        public boolean isDeclaration() {
            return isDeclaration;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public ExpressionTree getIterable() {
            return iterable;
        }
        @Override
        public StatementTree getBody() {
            return body;
        }
    }

    public static class BasicFunctionTree extends BasicCallableTree implements FunctionTree {
        public BlockTree body;
        public BasicFunctionTree(Location location, String name) {
            super(location, name);
        }
        public BasicFunctionTree(Location location, String name, Collection<Modifier> modifiers) {
            super(location, name, modifiers);
        }
        @Override
        public BlockTree getBody() {
            return body;
        }
    }

    public static class BasicGetTypeTree extends BasicUnaryExpressionTree implements GetTypeTree {
        public BasicGetTypeTree(Location location, ExpressionTree expression) {
            super(location, expression);
        }
    }

    public static class BasicIdentifierTree extends AbstractTree implements IdentifierTree {
        public String name;
        public BasicIdentifierTree(Location location, String name) {
            super(location);
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }

    public static class BasicIfElseTree extends AbstractTree implements IfElseTree {
        public ExpressionTree condition;
        public StatementTree ifBody, elseBody;
        public BasicIfElseTree(Location location) {
            super(location);
        }
        @Override
        public ExpressionTree getCondition() {
            return condition;
        }
        @Override
        public StatementTree getIfBody() {
            return ifBody;
        }
        @Override
        public StatementTree getElseBody() {
            return elseBody;
        }
    }

    public static class BasicImportTree extends AbstractTree implements ImportTree {
        public String[] path;
        public BasicImportTree(Location location, String... path) {
            super(location);
            this.path = Objects.requireNonNull(path);
        }
        @Override
        public String[] getPath() {
            return path;
        }

    }

    public static class BasicIntegerLiteralTree extends AbstractTree implements IntegerLiteralTree {
        public int i;
        public BasicIntegerLiteralTree(Location location, int i) {
            super(location);
            this.i = i;
        }
        @Override
        public Integer get() {
            return i;
        }
    }

    public static class BasicLambdaTree extends AbstractTree implements LambdaTree {
        public final List<ClosureTree> closures = new ArrayList<>();
        public final List<ParameterTree> parameters = new ArrayList<>();
        public BlockTree body;
        public BasicLambdaTree(Location location) {
            super(location);
        }
        @Override
        public List<ClosureTree> getClosures() {
            return closures;
        }
        @Override
        public List<ParameterTree> getParameters() {
            return parameters;
        }
        @Override
        public BlockTree getBody() {
            return body;
        }
    }

    public static class BasicMemberAccessTree extends BasicUnaryExpressionTree implements MemberAccessTree {
        public String member;
        public BasicMemberAccessTree(Location location) {
            super(location);
        }
        public BasicMemberAccessTree(Location location, ExpressionTree expression, String member) {
            super(location, expression);
            this.member = member;
        }
        @Override
        public String getMemberName() {
            return member;
        }
    }

    public static class BasicNamespaceTree extends BasicDefinitionTree implements NamespaceTree {
        public final List<DefinitionTree> definitions = new ArrayList<>();
        public BasicNamespaceTree(Location location, String name) {
            super(location, name);
        }
        public BasicNamespaceTree(Location location, String name, Collection<Modifier> modifiers) {
            super(location, name, modifiers);
        }
        @Override
        public List<DefinitionTree> getDefinitions() {
            return definitions;
        }
    }

    public static class BasicNativeFunctionTree extends BasicDefinitionTree implements NativeFunctionTree {
        public BasicNativeFunctionTree(Location location, String name) {
            super(location, name);
        }
        public BasicNativeFunctionTree(Location location, String name, Collection<Modifier> modifiers) {
            super(location, name, modifiers);
        }
    }

    public static class BasicAbstractMethodTree extends BasicDefinitionTree implements AbstractMethodTree {
        public BasicAbstractMethodTree(Location location, String name) {
            super(location, name);
        }
        public BasicAbstractMethodTree(Location location, String name, Collection<Modifier> modifiers) {
            super(location, name, modifiers);
        }
    }

    public static class BasicNotTree extends BasicUnaryExpressionTree implements NotTree {
        public BasicNotTree(Location location, ExpressionTree expression) {
            super(location, expression);
        }
    }

    public static class BasicNullLiteralTree extends AbstractTree implements NullLiteralTree {
        public BasicNullLiteralTree(Location location) {
            super(location);
        }
    }

    public static class BasicParameterTree extends AbstractTree implements ParameterTree {
        public String name;
        public boolean isConstant;
        public ExpressionTree initializer;
        public BasicParameterTree(Location location, boolean isConstant, String name) {
            super(location);
            this.isConstant = isConstant;
            this.name = name;
        }
        public BasicParameterTree(Location location, boolean isConstant, String name, ExpressionTree initializer) {
            super(location);
            this.isConstant = isConstant;
            this.name = name;
            this.initializer = initializer;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public boolean isConstant() {
            return isConstant;
        }
        @Override
        public ExpressionTree getInitializer() {
            return initializer;
        }
    }

    public static class BasicRangeTree extends AbstractTree implements RangeTree {
        public ExpressionTree from, to;
        public BasicRangeTree(Location location, ExpressionTree from, ExpressionTree to) {
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
    }

    public static class BasicReturnTree extends AbstractTree implements ReturnTree {
        public ExpressionTree expression;
        public BasicReturnTree(Location location) {
            super(location);
        }
        public BasicReturnTree(Location location, ExpressionTree expression) {
            super(location);
            this.expression = expression;
        }
        @Override
        public ExpressionTree getExpression() {
            return expression;
        }
    }

    public static class BasicRootTree extends AbstractTree implements RootTree {
        public final List<DefinitionTree> definitions = new ArrayList<>();
        public final List<StatementTree> statements = new ArrayList<>();
        public BasicRootTree() {
            super(null);
        }
        @Override
        public List<DefinitionTree> getDefinitions() {
            return definitions;
        }
        @Override
        public List<StatementTree> getStatements() {
            return statements;
        }
    }

    public static class BasicSignTree extends BasicUnaryExpressionTree implements SignTree {
        public boolean isNegation = true;
        public BasicSignTree(Location location) {
            super(location);
        }
        public BasicSignTree(Location location, ExpressionTree expression, boolean isNegation) {
            super(location, expression);
            this.isNegation = isNegation;
        }
        @Override
        public boolean isNegation() {
            return isNegation;
        }
    }

    public static class BasicStringLiteralTree extends AbstractTree implements StringLiteralTree {
        public String s;
        public BasicStringLiteralTree(Location location, String s) {
            super(location);
            this.s = s;
        }
        @Override
        public String get() {
            return s;
        }
    }

    public static class BasicSuperTree extends AbstractTree implements SuperTree {
        public String accessed;
        public BasicSuperTree(Location location, String accessed) {
            super(location);
            this.accessed = accessed;
        }
        @Override
        public String getName() {
            return accessed;
        }
    }

    public static class BasicThisTree extends AbstractTree implements ThisTree {
        public BasicThisTree(Location location) {
            super(location);
        }
    }

    public static class BasicThrowTree extends AbstractTree implements ThrowTree {
        public ExpressionTree thrown;
        public BasicThrowTree(Location location, ExpressionTree thrown) {
            super(location);
            this.thrown = thrown;
        }
        @Override
        public ExpressionTree getExpression() {
            return thrown;
        }
    }

    public static class BasicTryCatchTree extends AbstractTree implements TryCatchTree {
        public StatementTree tryBody, catchBody;
        public String exVarName;
        public BasicTryCatchTree(Location location) {
            super(location);
        }
        @Override
        public StatementTree getTryBody() {
            return tryBody;
        }
        @Override
        public String getExceptionName() {
            return exVarName;
        }
        @Override
        public StatementTree getCatchBody() {
            return catchBody;
        }
    }

    public static class BasicUseTree extends AbstractTree implements UseTree {
        public String name;
        public BasicUseTree(Location location, String name) {
            super(location);
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }

    public static class BasicVarDecTree extends BasicDefinitionTree implements VarDecTree {
        public boolean isConstant;
        public ExpressionTree initializer;
        public BasicVarDecTree(Location location, boolean isConstant, String name) {
            super(location, name);
            this.isConstant = isConstant;
        }
        public BasicVarDecTree(Location location, boolean isConstant, String name, ExpressionTree initializer) {
            super(location, name);
            this.isConstant = isConstant;
            this.initializer = initializer;
        }
        @Override
        public boolean isConstant() {
            return isConstant;
        }
        @Override
        public ExpressionTree getInitializer() {
            return initializer;
        }
    }

    public static class BasicMultiVarDecTree extends AbstractTree implements MultiVarDecTree {
        public final List<VarDecTree> declarations;
        public BasicMultiVarDecTree(Location location, List<VarDecTree> declarations) {
            super(location);
            this.declarations = declarations;
        }
        @Override
        public List<VarDecTree> getDeclarations() {
            return declarations;
        }
    }

    public static class BasicWhileDoTree extends AbstractTree implements WhileDoTree {

        public ExpressionTree condition;
        public StatementTree body;
        public BasicWhileDoTree(Location location) {
            super(location);
        }
        public BasicWhileDoTree(Location location, ExpressionTree condition, StatementTree body) {
            super(location);
            this.body = body;
            this.condition = condition;
        }
        @Override
        public StatementTree getBody() {
            return body;
        }
        @Override
        public ExpressionTree getCondition() {
            return condition;
        }
    }

    public static class BasicBreakPointTree extends AbstractTree implements BreakPointTree {

        public BasicBreakPointTree() {
            super(null);
        }

        @Override
        public <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
            return visitor.visitBreakPointTree(this, p);
        }
    }

}
