package com.tscript.runtime.jit;

import com.tscript.runtime.jit.BytecodeParser.*;

public abstract class ParentDelegationPhase<R> extends OptimizationPhase<R, Tree> {

    public ParentDelegationPhase(){
    }
    
    public ParentDelegationPhase(R defaultValue){
        super(defaultValue);
    }
    
    @Override
    public R visitRangeTree(RangeTree rangeTree, Tree parent) {
        return super.visitRangeTree(rangeTree, rangeTree);
    }

    @Override
    public R visitGetTypeTree(GetTypeTree getTypeTree, Tree parent) {
        return super.visitGetTypeTree(getTypeTree, getTypeTree);
    }

    @Override
    public R visitThrowTree(ThrowTree throwTree, Tree parent) {
        return super.visitThrowTree(throwTree, throwTree);
    }

    @Override
    public R visitForLoopTree(ForLoopTree forLoopTree, Tree parent) {
        return super.visitForLoopTree(forLoopTree, forLoopTree);
    }

    @Override
    public R visitIfElseTree(IfElseTree ifElseTree, Tree parent) {
        return super.visitIfElseTree(ifElseTree, ifElseTree);
    }

    @Override
    public R visitArgumentTree(ArgumentTree argumentTree, Tree parent) {
        return super.visitArgumentTree(argumentTree, argumentTree);
    }

    @Override
    public R visitBinaryOperationTree(BinaryOperationTree operationTree, Tree parent) {
        return super.visitBinaryOperationTree(operationTree, operationTree);
    }

    @Override
    public R visitReturnTree(ReturnTree returnTree, Tree parent) {
        return super.visitReturnTree(returnTree, returnTree);
    }

    @Override
    public R visitUnaryOperationTree(UnaryOperationTree operationTree, Tree parent) {
        return super.visitUnaryOperationTree(operationTree, operationTree);
    }

    @Override
    public R visitWriteContainerTree(WriteContainerTree writeTree, Tree parent) {
        return super.visitWriteContainerTree(writeTree, writeTree);
    }

    @Override
    public R visitCallTree(CallTree callTree, Tree parent) {
        return super.visitCallTree(callTree, callTree);
    }

    @Override
    public R visitBooleanTree(BooleanTree booleanTree, Tree parent) {
        return super.visitBooleanTree(booleanTree, booleanTree);
    }

    @Override
    public R visitIntegerTree(IntegerTree integerTree, Tree parent) {
        return super.visitIntegerTree(integerTree, integerTree);
    }

    @Override
    public R visitStringTree(StringTree stringTree, Tree tree) {
        return super.visitStringTree(stringTree, stringTree);
    }

    @Override
    public R visitTryCatchTree(TryCatchTree tryCatchTree, Tree parent) {
        return super.visitTryCatchTree(tryCatchTree, tryCatchTree);
    }

    @Override
    public R visitSequenceTree(SequenceTree sequenceTree, Tree parent) {
        return super.visitSequenceTree(sequenceTree, sequenceTree);
    }

    @Override
    public R visitRootTree(RootTree rootTree, Tree parent) {
        return super.visitRootTree(rootTree, rootTree);
    }

    @Override
    public R visitJavaCodeTree(JavaCodeTree javaCodeTree, Tree parent) {
        return super.visitJavaCodeTree(javaCodeTree, javaCodeTree);
    }

    @Override
    public R visitDictionaryTree(DictionaryTree dictionaryTree, Tree parent) {
        return super.visitDictionaryTree(dictionaryTree, dictionaryTree);
    }

    @Override
    public R visitThisTree(ThisTree thisTree, Tree parent) {
        return super.visitThisTree(thisTree, thisTree);
    }

    @Override
    public R visitArrayTree(ArrayTree arrayTree, Tree parent) {
        return super.visitArrayTree(arrayTree, arrayTree);
    }

    @Override
    public R visitCallSuperTree(CallSuperTree callSuperTree, Tree parent) {
        return super.visitCallSuperTree(callSuperTree, callSuperTree);
    }

    @Override
    public R visitConstantTree(ConstantTree constantTree, Tree parent) {
        return super.visitConstantTree(constantTree, constantTree);
    }

    @Override
    public R visitEqualsTree(EqualsTree equalsTree, Tree parent) {
        return super.visitEqualsTree(equalsTree, equalsTree);
    }

    @Override
    public R visitLoadAbstractImplTree(LoadAbstractImplTree loadTree, Tree parent) {
        return super.visitLoadAbstractImplTree(loadTree, loadTree);
    }

    @Override
    public R visitLoadGlobalTree(LoadGlobalTree loadGlobalTree, Tree parent) {
        return super.visitLoadGlobalTree(loadGlobalTree, loadGlobalTree);
    }

    @Override
    public R visitLoadLocalTree(LoadLocalTree loadLocalTree, Tree parent) {
        return super.visitLoadLocalTree(loadLocalTree, loadLocalTree);
    }

    @Override
    public R visitLoadMemberFastTree(LoadMemberFastTree loadTree, Tree parent) {
        return super.visitLoadMemberFastTree(loadTree, loadTree);
    }

    @Override
    public R visitLoadMemberTree(LoadMemberTree loadTree, Tree parent) {
        return super.visitLoadMemberTree(loadTree, loadTree);
    }

    @Override
    public R visitLoadStaticTree(LoadStaticTree loadTree, Tree parent) {
        return super.visitLoadStaticTree(loadTree, loadTree);
    }

    @Override
    public R visitNewLineTree(NewLineTree newLineTree, Tree parent) {
        return super.visitNewLineTree(newLineTree, newLineTree);
    }

    @Override
    public R visitNullTree(NullTree nullTree, Tree parent) {
        return super.visitNullTree(nullTree, nullTree);
    }

    @Override
    public R visitReadContainerTree(ReadContainerTree readTree, Tree parent) {
        return super.visitReadContainerTree(readTree, readTree);
    }

    @Override
    public R visitStoreGlobalTree(StoreGlobalTree storeGlobalTree, Tree parent) {
        return super.visitStoreGlobalTree(storeGlobalTree, storeGlobalTree);
    }

    @Override
    public R visitStoreLocalTree(StoreLocalTree storeLocalTree, Tree parent) {
        return super.visitStoreLocalTree(storeLocalTree, storeLocalTree);
    }

    @Override
    public R visitStoreMemberFastTree(StoreMemberFastTree storeTree, Tree parent) {
        return super.visitStoreMemberFastTree(storeTree, storeTree);
    }

    @Override
    public R visitStoreMemberTree(StoreMemberTree storeTree, Tree parent) {
        return super.visitStoreMemberTree(storeTree, storeTree);
    }

    @Override
    public R visitStoreStaticTree(StoreStaticTree storeTree, Tree parent) {
        return super.visitStoreStaticTree(storeTree, storeTree);
    }
}
