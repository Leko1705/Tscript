package runtime.jit;

import runtime.jit.BytecodeParser.*;

import java.util.Iterator;

public class TreeToStringVisitor extends SimpleTreeVisitor<String, Void> {

    int intent = 0;

    private String getIntent(){
        return " ".repeat(3).repeat(intent);
    }

    @Override
    public String visitRootTree(RootTree rootTree, Void unused) {
        return scan(rootTree.tree);
    }

    @Override
    public String visitSequenceTree(SequenceTree sequenceTree, Void unused) {
        return scanSequence(sequenceTree.children, getIntent(), "\n", "");
    }

    @Override
    public String visitStringTree(StringTree stringTree, Void unused) {
        return "\"" + stringTree.value + "\"";
    }

    @Override
    public String visitIntegerTree(IntegerTree integerTree, Void unused) {
        return Integer.toString(integerTree.value);
    }

    @Override
    public String visitRealTree(RealTree realTree, Void unused) {
        return Double.toString(realTree.value);
    }

    @Override
    public String visitBooleanTree(BooleanTree booleanTree, Void unused) {
        return Boolean.toString(booleanTree.value);
    }

    @Override
    public String visitNullTree(NullTree nullTree, Void unused) {
        return "null";
    }

    @Override
    public String visitGetTypeTree(GetTypeTree getTypeTree, Void unused) {
        return "typeof " + scan(getTypeTree.exp);
    }

    @Override
    public String visitArgumentTree(ArgumentTree argumentTree, Void unused) {
        return scan(argumentTree.exp);
    }

    @Override
    public String visitNewLineTree(NewLineTree newLineTree, Void unused) {
        return "new_line: " + newLineTree.line();
    }

    @Override
    public String visitReturnTree(ReturnTree returnTree, Void unused) {
        return "return " + scan(returnTree.expression);
    }

    @Override
    public String visitThisTree(ThisTree thisTree, Void unused) {
        return "this";
    }

    @Override
    public String visitJavaCodeTree(JavaCodeTree javaCodeTree, Void unused) {
        return javaCodeTree.code();
    }

    @Override
    public String visitEqualsTree(EqualsTree equalsTree, Void unused) {
        String eq = equalsTree.equals ? "==" : "!=";
        return scan(equalsTree.left) + eq + scan(equalsTree.right);
    }

    @Override
    public String visitRangeTree(RangeTree rangeTree, Void unused) {
        return scan(rangeTree.from) + ":" + scan(rangeTree.to);
    }

    @Override
    public String visitArrayTree(ArrayTree arrayTree, Void unused) {
        return scanSequence(arrayTree.arguments(), "[", ", ", "]");
    }

    @Override
    public String visitCallTree(CallTree callTree, Void unused) {
        return scanSequence(callTree.arguments, "(", ", ", ")");
    }

    @Override
    public String visitCallSuperTree(CallSuperTree callSuperTree, Void unused) {
        return "super" + scanSequence(callSuperTree.arguments(), "(", ", ", ")");
    }

    @Override
    public String visitThrowTree(ThrowTree throwTree, Void unused) {
        return "throw " + scan(throwTree.exp);
    }

    @Override
    public String visitStoreLocalTree(StoreLocalTree storeLocalTree, Void unused) {
        return "locals[" + storeLocalTree.address + "] = " + scan(storeLocalTree.child);
    }

    @Override
    public String visitLoadLocalTree(LoadLocalTree loadLocalTree, Void unused) {
        return "locals[" + loadLocalTree.address() + "]";
    }

    @Override
    public String visitForLoopTree(ForLoopTree forLoopTree, Void unused) {
        intent += 1;
        String storeCmd = forLoopTree.address < 0 ? "" : getIntent() + "locals[" + forLoopTree.address + "] = ITR_NEXT\n";
        String s = "for " + scan(forLoopTree.iterable) + ":\n" +
                storeCmd +
                scan(forLoopTree.body);
        intent -= 1;
        return s;
    }

    @Override
    public String visitBinaryOperationTree(BinaryOperationTree operationTree, Void unused) {
        return "(" + scan(operationTree.left) + " " + operationTree.operation + " " + scan(operationTree.right) + ")";
    }

    @Override
    public String visitUnaryOperationTree(UnaryOperationTree operationTree, Void unused) {
        return "(" + operationTree.operation + " " + scan(operationTree.exp) + ")";
    }

    @Override
    public String visitConstantTree(ConstantTree constantTree, Void unused) {
        return "constant[" + constantTree.address() + "]";
    }

    private String scanSequence(Iterable<Tree> sequence, String prefix, String suffix, String postFix){
        StringBuilder sb = new StringBuilder(prefix);
        Iterator<Tree> itr = sequence.iterator();
        if (itr.hasNext())
            sb.append(scan(itr.next()));
        while (itr.hasNext()){
            sb.append(suffix).append(scan(itr.next()));
        }
        return sb.append(postFix).toString();
    }
}
