package runtime.jit2.graph.tree;

import tscriptc.generation.Opcode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Trees {

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

}
