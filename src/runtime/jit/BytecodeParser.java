package runtime.jit;

import runtime.core.VirtualFunction;
import runtime.type.TInteger;
import runtime.type.TReal;
import runtime.type.TString;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.*;

class BytecodeParser {

    private BytecodeParser() {
    }

    private static BytecodeScanner scanner;
    private static VirtualFunction function;


    public static RootTree parse(VirtualFunction function) {
        BytecodeParser.function = function;
        scanner = new BytecodeScanner(function);
        skipParameterStorage();
        return new RootTree(scanSequence(Integer.MAX_VALUE));
    }

    private static SequenceTree scanSequence(int maxIndex){
        final ArrayDeque<Tree> stack = new ArrayDeque<>();
        SequenceTree sequence = new SequenceTree();

        for (; scanner.hasNext() && scanner.getIp() < maxIndex; scanner.consume()) {
            Opcode opcode = scanner.peekOpcode();

            switch (opcode) {
                case PUSH_NULL -> stack.push(new NullTree());
                case PUSH_INT -> stack.push(new IntegerTree((int) scanner.getArg(0)));
                case PUSH_BOOL -> stack.push(new BooleanTree(scanner.getArg(0)));
                case RETURN -> sequence.children.add(new ReturnTree(stack.pop()));
                case LOAD_CONST -> {
                    int address = scanner.getArg(0);
                    Object poolObj = function.getPool().load(address, null);
                    Tree loaded;
                    if (poolObj instanceof TInteger i){
                        loaded = new IntegerTree(i.get());
                    }
                    else if (poolObj instanceof TReal r){
                        loaded = new RealTree(r.get());
                    }
                    else if (poolObj instanceof TString s){
                        loaded = new StringTree(s.get());
                    }
                    else {
                        loaded = new ConstantTree(address);
                    }
                    stack.push(loaded);
                }
                case STORE_LOCAL -> sequence.children.add(new StoreLocalTree(scanner.getArg(0), stack.pop()));
                case LOAD_LOCAL -> stack.push(new LoadLocalTree(scanner.getArg(0)));
                case STORE_GLOBAL -> sequence.children.add(new StoreGlobalTree(scanner.getArg(0), stack.pop()));
                case LOAD_GLOBAL -> stack.push(new LoadGlobalTree(scanner.getArg(0)));
                case ADD, SUB, MUL, DIV,
                        IDIV, MOD, POW, AND,
                        OR, SLA, SRA, SRL,
                        XOR, LT, GT, GEQ, LEQ -> {
                    Tree right = stack.pop();
                    Tree left = stack.pop();
                    Tree t = new BinaryOperationTree(left, right, opcode);
                    stack.push(t);
                }
                case EQUALS, NOT_EQUALS -> {
                    Tree right = stack.pop();
                    Tree left = stack.pop();
                    Tree t = new EqualsTree(left, right, opcode, opcode == Opcode.EQUALS);
                    stack.push(t);
                }
                case NEG, POS, NOT -> {
                    Tree t = new UnaryOperationTree(stack.pop(), opcode);
                    stack.push(t);
                }
                case PUSH_THIS -> stack.push(new ThisTree());
                case CALL -> {
                    CallTree callTree = new CallTree(stack.pop(), new ArrayList<>());
                    LinkedList<Tree> trees = new LinkedList<>();
                    for (int i = 0; i < scanner.getArg(0); i++)
                        trees.addFirst(stack.pop());
                    callTree.arguments.addAll(trees);
                    stack.push(callTree);
                }
                case CALL_SUPER -> {
                    CallSuperTree callTree = new CallSuperTree(new ArrayList<>());
                    LinkedList<Tree> trees = new LinkedList<>();
                    for (int i = 0; i < scanner.getArg(0); i++)
                        trees.addFirst(stack.pop());
                    callTree.arguments.addAll(trees);
                    stack.push(callTree);
                }
                case POP -> sequence.children.add(stack.pop());
                case WRAP_ARGUMENT -> stack.push(new ArgumentTree(scanner.getArg(0), stack.pop()));
                case GET_TYPE -> stack.push(new GetTypeTree(stack.pop()));
                case THROW -> sequence.children.add(new ThrowTree(stack.pop()));
                case MAKE_ARRAY -> {
                    ArrayTree arrayTree = new ArrayTree(new ArrayList<>());
                    LinkedList<Tree> trees = new LinkedList<>();
                    for (int i = 0; i < scanner.getArg(0); i++)
                        trees.addFirst(stack.pop());
                    arrayTree.arguments.addAll(trees);
                    stack.push(arrayTree);
                }
                case MAKE_DICT -> {
                    LinkedHashMap<Tree, Tree> content = new LinkedHashMap<>();
                    int count = scanner.getArg(0);
                    for (; count > 0; count--) {
                        Tree key = stack.pop();
                        Tree value = stack.pop();
                        content.put(key, value);
                    }
                    stack.push(new DictionaryTree(content));
                }
                case MAKE_RANGE -> {
                    Tree to = stack.pop();
                    Tree from = stack.pop();
                    stack.push(new RangeTree(from, to));
                }
                case NEW_LINE -> {
                    byte[] bytes = scanner.getInstruction();
                    sequence.children.add(new NewLineTree(Conversion.fromBytes(
                            bytes[1],
                            bytes[2],
                            bytes[3],
                            bytes[4])));
                }
                case STORE_MEMBER_FAST -> sequence.children.add(new StoreMemberFastTree(scanner.getArg(0), stack.pop()));
                case LOAD_MEMBER_FAST -> stack.push(new LoadMemberFastTree(scanner.getArg(0)));
                case STORE_MEMBER -> {
                    String name = (String) function.getPool().load(scanner.getArg(0), null);
                    stack.push(new StoreMemberTree(name, stack.pop()));
                }
                case LOAD_MEMBER -> {
                    String name = (String) function.getPool().load(scanner.getArg(0), null);
                    stack.push(new LoadMemberTree(name, stack.pop()));
                }
                case STORE_STATIC -> {
                    String name = (String) function.getPool().load(scanner.getArg(0), null);
                    stack.push(new StoreStaticTree(name, stack.pop()));
                }
                case LOAD_STATIC -> {
                    String name = (String) function.getPool().load(scanner.getArg(0), null);
                    stack.push(new LoadStaticTree(name));
                }
                case CONTAINER_WRITE -> {
                    Tree container = stack.pop();
                    Tree key = stack.pop();
                    Tree value = stack.pop();
                    WriteContainerTree writeTree = new WriteContainerTree(container, key, value);
                    sequence.children.add(writeTree);
                }
                case CONTAINER_READ -> {
                    Tree container = stack.pop();
                    Tree key = stack.pop();
                    ReadContainerTree readTree = new ReadContainerTree(container, key);
                    stack.push(readTree);
                }
                case LOAD_ABSTRACT_IMPL -> {
                    String name = (String) function.getPool().load(scanner.getArg(0), null);
                    stack.push(new LoadAbstractImplTree(name));
                }
                case ENTER_TRY -> {
                    scanner.consume();
                    SequenceTree tryBody = scanSequence(Integer.MAX_VALUE);
                    assertOpcode(Opcode.LEAVE_TRY);
                    scanner.consume();
                    assertOpcode(Opcode.GOTO);
                    int jumpAddress = toJumpIndex(scanner.getArg(0), scanner.getArg(1));
                    scanner.consume();
                    assertOpcode(Opcode.STORE_LOCAL);
                    int localAddress = scanner.getArg(0);
                    scanner.consume();

                    SequenceTree catchBody = new SequenceTree();
                    if (jumpAddress != scanner.getIp())
                        catchBody = scanSequence(jumpAddress);
                    else scanner.pushBack();
                    sequence.children.add(new TryCatchTree(tryBody, catchBody, localAddress));
                }
                case LEAVE_TRY -> {
                    return sequence;
                }
                case GET_ITR -> {
                    Tree iterable = stack.pop();
                    scanner.consume();
                    assertOpcode(Opcode.BRANCH_ITR);
                    int jumpAddress = toJumpIndex(scanner.getArg(0), scanner.getArg(1));
                    scanner.consume();
                    assertOpcode(Opcode.ITR_NEXT);
                    scanner.consume();
                    assertOpcode(Opcode.POP, Opcode.STORE_LOCAL);
                    Opcode nextHandling = scanner.peekOpcode();
                    int storeAddress = nextHandling == Opcode.POP ? -1 : scanner.getArg(0);
                    scanner.consume();
                    SequenceTree body = scanSequence(jumpAddress-1);
                    sequence.children.add(new ForLoopTree(iterable, body, storeAddress));
                }
                case ITR_NEXT, BRANCH_ITR -> {
                    throw new IllegalStateException("should never be handled");
                }
                case BRANCH_IF_TRUE, BRANCH_IF_FALSE -> {
                    int jumpAddress = toJumpIndex(scanner.getArg(0), scanner.getArg(1));
                    if (scanner.getIp() < jumpAddress){
                        IfElseTree ifElseTree = parseIfElseTree(stack.pop(), opcode == Opcode.BRANCH_IF_FALSE, jumpAddress);
                        sequence.children.add(ifElseTree);
                    }
                }
            }

        }

        return sequence;
    }

    private static IfElseTree parseIfElseTree(Tree condition, boolean ifTrue, int jumpAddress){
        assertOpcode(Opcode.BRANCH_IF_TRUE, Opcode.BRANCH_IF_FALSE);
        scanner.consume();

        SequenceTree ifBody = new SequenceTree();
        if (jumpAddress - scanner.getIp() > 1)
            ifBody = scanSequence(jumpAddress);

        SequenceTree elseBody = null;
        Opcode opcode = scanner.peekOpcode();
        if (opcode == Opcode.GOTO){
            jumpAddress = toJumpIndex(scanner.getArg(0), scanner.getArg(1));
            scanner.consume();
            if (jumpAddress > scanner.getIp()){
                scanner.pushBack();
                elseBody = scanSequence(jumpAddress);
            }
            else scanner.pushBack();
        }

        scanner.pushBack();
        return new IfElseTree(condition, ifTrue, ifBody, elseBody);
    }

    private static int toJumpIndex(byte b1, byte b2){
        return ((b1 & 0xff) << 8) | (b2 & 0xff);
    }

    private static void skipParameterStorage() {
        while (scanner.hasNext()) {
            Opcode opcode = scanner.peekOpcode();
            if (opcode != Opcode.STORE_LOCAL) break;
            scanner.consume();
        }
    }

    public static void assertOpcode(Opcode... expected){
        for (Opcode o : expected)
            if (o == scanner.peekOpcode())
                return;
        throw new AssertionError("expected " + List.of(expected) + " but got " + scanner.peekOpcode());
    }


    private static String intent(int depth){
        return " |-".repeat(4).repeat(depth);
    }

    public interface Tree {
        <R, P> R accept(TreeVisitor<R, P> visitor, P p);

        default <R, P> R accept(TreeVisitor<R, P> visitor) {
            return accept(visitor, null);
        }
        default void replace(Tree toReplace, Tree tree) {}
        default void remove(Tree tree){};

    }

    public interface IterableTree extends Tree {}

    public static class RootTree implements Tree {
        public Tree tree;
        public RootTree(Tree child){
            this.tree = child;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitRootTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            this.tree = tree;
        }

        @Override
        public String toString() {
            return accept(new TreeToStringVisitor());
        }
    }

    public record JavaCodeTree(String code) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitJavaCodeTree(this, p);
        }
    }

    public record NewLineTree(int line) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitNewLineTree(this, p);
        }
    }

    public static class SequenceTree implements Tree {
        public List<Tree> children = new ArrayList<>();

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitSequenceTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            children.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
        }
        @Override
        public void remove(Tree tree) {
            children.removeIf(candidate -> candidate == tree);
        }
    }

    public static class ReturnTree implements Tree {
        public Tree expression;
        public ReturnTree(Tree expression){
            this.expression = expression;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitReturnTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            expression = tree;
        }
    }

    public static abstract class LiteralTree<T> implements Tree {
        public T value;

        public LiteralTree(T value) {
            this.value = value;
        }
    }

    public static class NullTree extends LiteralTree<Void> {
        public NullTree() {
            super(null);
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitNullTree(this, p);
        }
    }

    public static class IntegerTree extends LiteralTree<Integer> {
        public IntegerTree(Integer value) {
            super(value);
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitIntegerTree(this, p);
        }
    }

    public static class RealTree extends LiteralTree<Double> {
        public RealTree(double value) {
            super(value);
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitRealTree(this, p);
        }
    }

    public static class BooleanTree extends LiteralTree<Boolean> {
        public BooleanTree(Boolean value) {
            super(value);
        }

        public BooleanTree(int intVal) {
            super(intVal != 0);
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitBooleanTree(this, p);
        }
    }

    public static class StringTree extends LiteralTree<String> {
        public StringTree(String value) {
            super(value);
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitStringTree(this, p);
        }
    }

    public record ConstantTree(int address) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitConstantTree(this, p);
        }
    }

    public record ArrayTree(List<Tree> arguments) implements IterableTree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitArrayTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            arguments.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
        }
    }

    public static class DictionaryTree implements IterableTree {

        public LinkedHashMap<Tree, Tree> arguments;

        public DictionaryTree(LinkedHashMap<Tree, Tree> arguments){
            this.arguments = arguments;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitDictionaryTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (arguments.containsKey(toReplace)) {
                LinkedHashMap<Tree, Tree> newMap = new LinkedHashMap<>();
                for (Tree old : arguments.keySet()){
                    Tree newKey = old == toReplace ? tree : old;
                    arguments.put(newKey, arguments.get(old));
                    if (newKey == tree) break;
                }
                arguments = newMap;
            }
            else {
                for (Map.Entry<Tree, Tree> entry : arguments.entrySet()){
                    if (toReplace == entry.getValue()){
                        entry.setValue(tree);
                        break;
                    }
                }
            }
        }
    }

    public static class RangeTree implements IterableTree {
        public Tree from, to;

        public RangeTree(Tree from, Tree to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitRangeTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (toReplace == from) from = tree;
            else if (toReplace == to) to = tree;
        }
    }

    public record LoadLocalTree(int address) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitLoadLocalTree(this, p);
        }
    }

    public static class StoreLocalTree implements Tree {

        public final int address;
        public Tree child;

        public StoreLocalTree(int address, Tree child) {
            this.address = address;
            this.child = child;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitStoreLocalTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            child = tree;
        }
    }

    public record LoadGlobalTree(int address) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitLoadGlobalTree(this, p);
        }
    }

    public static class StoreGlobalTree implements Tree {

        public final int address;
        public Tree child;

        public StoreGlobalTree(int address, Tree child) {
            this.address = address;
            this.child = child;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitStoreGlobalTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            child = tree;
        }
    }

    public static abstract class BinaryExpressionTree implements Tree {
        public Tree left;
        public Tree right;

        public BinaryExpressionTree(Tree left, Tree right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (toReplace == left) left = tree;
            if (toReplace == right) right = tree;
        }
    }

    public static class BinaryOperationTree extends BinaryExpressionTree {

        public final Opcode operation;

        public BinaryOperationTree(Tree left, Tree right, Opcode operation) {
            super(left, right);
            this.operation = operation;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitBinaryOperationTree(this, p);
        }
    }

    public static class EqualsTree extends BinaryOperationTree {
        public final boolean equals;

        public EqualsTree(Tree left, Tree right, Opcode operation, boolean equals) {
            super(left, right, operation);
            this.equals = equals;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitEqualsTree(this, p);
        }
    }

    public static class UnaryOperationTree implements Tree {
        public Tree exp;
        public final Opcode operation;

        public UnaryOperationTree(Tree exp, Opcode operation) {
            this.exp = exp;
            this.operation = operation;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitUnaryOperationTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            exp = tree;
        }
    }

    public static class ThisTree implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitThisTree(this, p);
        }
    }

    public static class CallTree implements Tree {
        public Tree called;
        public final List<Tree> arguments;

        public CallTree(Tree called, List<Tree> arguments) {
            this.called = called;
            this.arguments = arguments;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitCallTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            arguments.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
        }
    }

    public record CallSuperTree(List<Tree> arguments) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitCallSuperTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            arguments.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
        }
    }

    public static class ArgumentTree implements Tree {
        public final int address;
        public Tree exp;

        public ArgumentTree(int address, Tree exp) {
            this.address = address;
            this.exp = exp;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitArgumentTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            exp = tree;
        }
    }

    public static class GetTypeTree implements Tree {
        public Tree exp;

        public GetTypeTree(Tree exp) {
            this.exp = exp;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitGetTypeTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            exp = tree;
        }
    }

    public static class ThrowTree implements Tree {
        public Tree exp;

        public ThrowTree(Tree exp) {
            this.exp = exp;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitThrowTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            exp = tree;
        }
    }

    public record LoadMemberFastTree(int address) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitLoadMemberFastTree(this, p);
        }
    }

    public static class StoreMemberFastTree implements Tree {
        public final int address;
        public Tree child;
        public StoreMemberFastTree(int address, Tree child) {
            this.address = address;
            this.child = child;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitStoreMemberFastTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            child = tree;
        }
    }

    public static class LoadMemberTree implements Tree {

        public String address;
        public Tree exp;
        public LoadMemberTree(String address, Tree exp){
            this.address = address;
            this.exp = exp;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitLoadMemberTree(this, p);
        }
    }

    public static class AccessUnknownFastTree implements Tree {

        public int address;
        public Tree exp;
        public AccessUnknownFastTree(int address, Tree exp){
            this.address = address;
            this.exp = exp;
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitAccessUnknownFastTree(this, p);
        }
    }

    public static class StoreMemberTree implements Tree {
        public final String address;
        public Tree child;
        public StoreMemberTree(String address, Tree child) {
            this.address = address;
            this.child = child;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitStoreMemberTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            child = tree;
        }
    }

    public record LoadStaticTree(String address) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitLoadStaticTree(this, p);
        }
    }

    public static class StoreStaticTree implements Tree {
        public final String address;
        public Tree child;
        public StoreStaticTree(String address, Tree child) {
            this.address = address;
            this.child = child;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitStoreStaticTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            child = tree;
        }
    }

    public static class WriteContainerTree implements Tree {
        public Tree container, key, exp;
        public WriteContainerTree(Tree container, Tree key, Tree exp){
            this.container = container;
            this.key = key;
            this.exp = exp;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitWriteContainerTree(this, p);
        }
        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (toReplace == container) container = tree;
            else if (toReplace == key) key = tree;
            else if (toReplace == exp) exp = tree;
        }
    }

    public static class ReadContainerTree implements Tree {
        public Tree container, key;
        public ReadContainerTree(Tree container, Tree key){
            this.container = container;
            this.key = key;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitReadContainerTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (toReplace == container) container = tree;
            else if (toReplace == key) key = tree;
        }
    }

    public record LoadAbstractImplTree(String name) implements Tree {
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitLoadAbstractImplTree(this, p);
        }
    }

    public static class TryCatchTree implements Tree {
        Tree tryBody, catchBody;
        int exAddress;
        public TryCatchTree(Tree tryBody, Tree catchBody, int exAddress){
            this.tryBody = tryBody;
            this.catchBody = catchBody;
            this.exAddress = exAddress;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitTryCatchTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (toReplace == tryBody) tryBody = tree;
            else if (toReplace == catchBody) catchBody = tree;
        }
    }

    public static class ForLoopTree implements Tree {
        public Tree iterable, body;
        public int address = -1;
        public ForLoopTree(Tree iterable, Tree body, int address){
            this.iterable = iterable;
            this.body = body;
            this.address = address;
        }
        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitForLoopTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (toReplace == iterable) iterable = tree;
            else if (toReplace == body) body = tree;
        }
    }

    public static class IfElseTree implements Tree {

        public Tree condition, ifBody, elseBody;

        public boolean ifTrue;

        public IfElseTree(Tree condition, boolean ifTrue, Tree ifBody, Tree elseBody){
            this.condition = condition;
            this.ifBody = ifBody;
            this.elseBody = elseBody;
            this.ifTrue = ifTrue;
        }

        public IfElseTree(Tree condition, boolean ifTrue, Tree ifBody){
            this(condition, ifTrue, ifBody, null);
        }

        @Override
        public <R, P> R accept(TreeVisitor<R, P> visitor, P p) {
            return visitor.visitIfElseTree(this, p);
        }

        @Override
        public void replace(Tree toReplace, Tree tree) {
            if (toReplace == condition)
                condition = tree;
            if (ifBody == toReplace)
                ifBody = tree;
            if (elseBody != null && elseBody == toReplace)
                elseBody = tree;
        }
    }

}
