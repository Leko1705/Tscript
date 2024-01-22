package runtime.jit;
import runtime.jit.BytecodeParser.*;

import java.util.List;

public class SimpleTreeVisitor<R, P> implements TreeVisitor<R, P> {
    
    private final R defaultValue;

    public SimpleTreeVisitor(){
        defaultValue = null;
    }
    
    public SimpleTreeVisitor(R defaultValue){
        this.defaultValue = defaultValue;
    }

    public R getDefaultValue() {
        return defaultValue;
    }

    public R selectResult(R r1, R r2){
        return r1;
    }

    public R scan(Tree tree, P p){
        return (tree != null) ? tree.accept(this, p) : defaultValue;
    }

    public R scan(Tree tree){
        return scan(tree, null);
    }

    public R scan(List<? extends Tree> trees, P p){
        R r = defaultValue;
        for (Tree tree : trees)
            r = scanSelective(tree, p, r);
        return r;
    }

    public R scanSelective(Tree tree, P p, R r){
        return selectResult(scan(tree, p), r);
    }

    public R scanSelective(List<? extends Tree> trees, P p, R r){
        for (Tree tree : trees)
            r = scanSelective(tree, p, r);
        return r;
    }
    @Override
    public R visitRootTree(RootTree rootTree, P p) {
        return scan(rootTree.tree, p);
    }

    @Override
    public R visitSequenceTree(SequenceTree sequenceTree, P p) {
        return scan(sequenceTree.children, p);
    }

    @Override
    public R visitReturnTree(ReturnTree returnTree, P p) {
        return scan(returnTree.expression, p);
    }

    @Override
    public R visitNullTree(NullTree nullTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitIntegerTree(IntegerTree integerTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitRealTree(RealTree realTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitBooleanTree(BooleanTree booleanTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitStringTree(StringTree stringTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitConstantTree(ConstantTree constantTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitLoadLocalTree(LoadLocalTree loadLocalTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitStoreLocalTree(StoreLocalTree storeLocalTree, P p) {
        return scan(storeLocalTree.child, p);
    }

    @Override
    public R visitLoadGlobalTree(LoadGlobalTree loadGlobalTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitStoreGlobalTree(StoreGlobalTree storeGlobalTree, P p) {
        return scan(storeGlobalTree.child, p);
    }

    @Override
    public R visitBinaryOperationTree(BinaryOperationTree operationTree, P p) {
        R r = scan(operationTree.left, p);
        r = scanSelective(operationTree.right, p, r);
        return r;
    }

    @Override
    public R visitUnaryOperationTree(UnaryOperationTree operationTree, P p) {
        return scan(operationTree.exp, p);
    }

    @Override
    public R visitThisTree(ThisTree thisTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitEqualsTree(EqualsTree equalsTree, P p) {
        R r = scan(equalsTree.left, p);
        r = scanSelective(equalsTree.right, p, r);
        return r;
    }

    @Override
    public R visitCallTree(CallTree callTree, P p) {
        R r = scan(callTree.called, p);
        r = scanSelective(callTree.arguments, p, r);
        return r;
    }

    @Override
    public R visitCallSuperTree(CallSuperTree callSuperTree, P p) {
        return scan(callSuperTree.arguments(), p);
    }

    @Override
    public R visitArgumentTree(ArgumentTree argumentTree, P p) {
        return scan(argumentTree.exp, p);
    }

    @Override
    public R visitGetTypeTree(GetTypeTree getTypeTree, P p) {
        return scan(getTypeTree.exp, p);
    }

    @Override
    public R visitThrowTree(ThrowTree throwTree, P p) {
        return scan(throwTree.exp, p);
    }

    @Override
    public R visitArrayTree(ArrayTree arrayTree, P p) {
        return scan(arrayTree.arguments(), p);
    }

    @Override
    public R visitDictionaryTree(DictionaryTree dictionaryTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitRangeTree(RangeTree rangeTree, P p) {
        R r = scan(rangeTree.from, p);
        r = scanSelective(rangeTree.to, p, r);
        return r;
    }

    @Override
    public R visitNewLineTree(NewLineTree newLineTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitLoadMemberFastTree(LoadMemberFastTree loadTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitStoreMemberFastTree(StoreMemberFastTree storeTree, P p) {
        return scan(storeTree.child, p);
    }

    @Override
    public R visitLoadMemberTree(LoadMemberTree loadTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitStoreMemberTree(StoreMemberTree storeTree, P p) {
        return scan(storeTree.child, p);
    }

    @Override
    public R visitAccessUnknownFastTree(AccessUnknownFastTree accessTre, P p) {
        return defaultValue;
    }

    @Override
    public R visitLoadStaticTree(LoadStaticTree loadTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitStoreStaticTree(StoreStaticTree storeTree, P p) {
        return scan(storeTree.child, p);
    }

    @Override
    public R visitWriteContainerTree(WriteContainerTree writeTree, P p) {
        R r = scan(writeTree.container, p);
        r = scanSelective(writeTree.key, p, r);
        r = scanSelective(writeTree.exp, p, r);
        return r;
    }

    @Override
    public R visitReadContainerTree(ReadContainerTree readTree, P p) {
        R r = scan(readTree.container, p);
        r = scanSelective(readTree.key, p, r);
        return r;
    }

    @Override
    public R visitLoadAbstractImplTree(LoadAbstractImplTree loadTree, P p) {
        return defaultValue;
    }

    @Override
    public R visitTryCatchTree(TryCatchTree tryCatchTree, P p) {
        R r = scan(tryCatchTree.tryBody, p);
        r = scanSelective(tryCatchTree.catchBody, p, r);
        return r;
    }

    @Override
    public R visitForLoopTree(ForLoopTree forLoopTree, P p) {
        R r = scan(forLoopTree.iterable, p);
        r = scanSelective(forLoopTree.body, p, r);
        return r;
    }

    @Override
    public R visitIfElseTree(IfElseTree ifElseTree, P p) {
        R r = scan(ifElseTree.condition, p);
        r = scanSelective(ifElseTree.ifBody, p, r);
        r = scanSelective(ifElseTree.elseBody, p, r);
        return r;
    }

    @Override
    public R visitJavaCodeTree(JavaCodeTree javaCodeTree, P p) {
        return defaultValue;
    }
}
