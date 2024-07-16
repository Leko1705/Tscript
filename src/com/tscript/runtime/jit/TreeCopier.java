package com.tscript.runtime.jit;

import com.tscript.runtime.jit.BytecodeParser.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class TreeCopier extends SimpleTreeVisitor<Tree, Void> {

    private static final TreeCopier copier = new TreeCopier();

    public static Tree copy(Tree tree){
        return tree.accept(copier);
    }

    private TreeCopier(){
    }

    @Override
    public Tree visitRootTree(RootTree rootTree, Void unused) {
        return new RootTree(scan(rootTree.tree));
    }

    @Override
    public Tree visitSequenceTree(SequenceTree sequenceTree, Void unused) {
        SequenceTree sequence = new SequenceTree();
        for (Tree child : sequenceTree.children)
            sequence.children.add(scan(child));
        return sequence;
    }

    @Override
    public Tree visitReturnTree(ReturnTree returnTree, Void unused) {
        return new ReturnTree(scan(returnTree.expression));
    }

    @Override
    public Tree visitNullTree(NullTree nullTree, Void unused) {
        return new NullTree();
    }

    @Override
    public Tree visitIntegerTree(IntegerTree integerTree, Void unused) {
        return new IntegerTree(integerTree.value);
    }

    @Override
    public Tree visitBooleanTree(BooleanTree booleanTree, Void unused) {
        return new BooleanTree(booleanTree.value);
    }

    @Override
    public Tree visitConstantTree(ConstantTree constantTree, Void unused) {
        return new ConstantTree(constantTree.address());
    }

    @Override
    public Tree visitLoadLocalTree(LoadLocalTree loadLocalTree, Void unused) {
        return new LoadLocalTree(loadLocalTree.address());
    }

    @Override
    public Tree visitStoreLocalTree(StoreLocalTree storeLocalTree, Void unused) {
        return new StoreLocalTree(storeLocalTree.address, scan(storeLocalTree.child));
    }

    @Override
    public Tree visitLoadGlobalTree(LoadGlobalTree loadGlobalTree, Void unused) {
        return new LoadGlobalTree(loadGlobalTree.address());
    }

    @Override
    public Tree visitStoreGlobalTree(StoreGlobalTree storeGlobalTree, Void unused) {
        return new StoreGlobalTree(storeGlobalTree.address, scan(storeGlobalTree.child));
    }

    @Override
    public Tree visitBinaryOperationTree(BinaryOperationTree operationTree, Void unused) {
        return new BinaryOperationTree(scan(operationTree.left), scan(operationTree.right), operationTree.operation);
    }

    @Override
    public Tree visitUnaryOperationTree(UnaryOperationTree operationTree, Void unused) {
        return new UnaryOperationTree(scan(operationTree.exp), operationTree.operation);
    }

    @Override
    public Tree visitThisTree(ThisTree thisTree, Void unused) {
        return new ThisTree();
    }

    @Override
    public Tree visitEqualsTree(EqualsTree equalsTree, Void unused) {
        return new EqualsTree(scan(equalsTree.left), scan(equalsTree.right), equalsTree.operation, equalsTree.equals);
    }

    @Override
    public Tree visitCallTree(CallTree callTree, Void unused) {
        CallTree callTree1 = new CallTree(scan(callTree.called), new LinkedList<>());
        for (Tree arg : callTree.arguments)
            callTree1.arguments.add(scan(arg));
        return callTree1;
    }

    @Override
    public Tree visitCallSuperTree(CallSuperTree callSuperTree, Void unused) {
        CallSuperTree callTree1 = new CallSuperTree(new LinkedList<>());
        for (Tree arg : callTree1.arguments())
            callTree1.arguments().add(scan(arg));
        return callTree1;
    }

    @Override
    public Tree visitArgumentTree(ArgumentTree argumentTree, Void unused) {
        return new ArgumentTree(argumentTree.address, scan(argumentTree.exp));
    }

    @Override
    public Tree visitGetTypeTree(GetTypeTree getTypeTree, Void unused) {
        return new GetTypeTree(scan(getTypeTree.exp));
    }

    @Override
    public Tree visitThrowTree(ThrowTree throwTree, Void unused) {
        return new ThrowTree(scan(throwTree.exp));
    }

    @Override
    public Tree visitArrayTree(ArrayTree arrayTree, Void unused) {
        ArrayTree arrayTree1 = new ArrayTree(new LinkedList<>());
        for (Tree arg : arrayTree.arguments())
            arrayTree1.arguments().add(scan(arg));
        return arrayTree1;
    }

    @Override
    public Tree visitDictionaryTree(DictionaryTree dictionaryTree, Void unused) {
        DictionaryTree dictionaryTree1 = new DictionaryTree(new LinkedHashMap<>());
        for (Map.Entry<Tree, Tree> entry : dictionaryTree.arguments.entrySet())
            dictionaryTree1.arguments.put(scan(entry.getKey()), scan(entry.getValue()));
        return dictionaryTree1;
    }

    @Override
    public Tree visitRangeTree(RangeTree rangeTree, Void unused) {
        return new RangeTree(scan(rangeTree.from), scan(rangeTree.to));
    }

    @Override
    public Tree visitNewLineTree(NewLineTree newLineTree, Void unused) {
        return new NewLineTree(newLineTree.line());
    }

    @Override
    public Tree visitLoadMemberFastTree(LoadMemberFastTree loadTree, Void unused) {
        return new LoadMemberFastTree(loadTree.address());
    }

    @Override
    public Tree visitStoreMemberFastTree(StoreMemberFastTree storeTree, Void unused) {
        return new StoreMemberFastTree(storeTree.address, scan(storeTree.child));
    }

    @Override
    public Tree visitLoadMemberTree(LoadMemberTree loadTree, Void unused) {
        return new LoadMemberTree(loadTree.address, scan(loadTree.exp));
    }

    @Override
    public Tree visitStoreMemberTree(StoreMemberTree storeTree, Void unused) {
        return new StoreMemberTree(storeTree.address, scan(storeTree.child));
    }

    @Override
    public Tree visitLoadStaticTree(LoadStaticTree loadTree, Void unused) {
        return new LoadStaticTree(loadTree.address());
    }

    @Override
    public Tree visitStoreStaticTree(StoreStaticTree storeTree, Void unused) {
        return new StoreStaticTree(storeTree.address, scan(storeTree.child));
    }

    @Override
    public Tree visitWriteContainerTree(WriteContainerTree writeTree, Void unused) {
        return new WriteContainerTree(scan(writeTree.container), scan(writeTree.key), scan(writeTree.exp));
    }

    @Override
    public Tree visitReadContainerTree(ReadContainerTree readTree, Void unused) {
        return new ReadContainerTree(scan(readTree.container), scan(readTree.key));
    }

    @Override
    public Tree visitLoadAbstractImplTree(LoadAbstractImplTree loadTree, Void unused) {
        return new LoadAbstractImplTree(loadTree.name());
    }

    @Override
    public Tree visitTryCatchTree(TryCatchTree tryCatchTree, Void unused) {
        return new TryCatchTree(scan(tryCatchTree.tryBody), scan(tryCatchTree.catchBody), tryCatchTree.exAddress);
    }

    @Override
    public Tree visitForLoopTree(ForLoopTree forLoopTree, Void unused) {
        return new ForLoopTree(scan(forLoopTree.iterable), scan(forLoopTree.body), forLoopTree.address);
    }

    @Override
    public Tree visitIfElseTree(IfElseTree ifElseTree, Void unused) {
        return new IfElseTree(scan(ifElseTree.condition), ifElseTree.ifTrue, scan(ifElseTree.ifBody), scan(ifElseTree.elseBody));
    }

    @Override
    public Tree visitJavaCodeTree(JavaCodeTree javaCodeTree, Void unused) {
        return javaCodeTree; // tree is immutable
    }
}
