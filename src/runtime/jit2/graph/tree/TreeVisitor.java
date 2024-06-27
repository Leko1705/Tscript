package runtime.jit2.graph.tree;

import runtime.jit2.graph.tree.Trees.*;

public interface TreeVisitor<R, P> {

    R visitRootTree(RootTree rootTree, P p);

    R visitSequenceTree(SequenceTree sequenceTree, P p);

    R visitReturnTree(ReturnTree returnTree, P p);

    R visitNullTree(NullTree nullTree, P p);

    R visitIntegerTree(IntegerTree integerTree, P p);

    R visitRealTree(RealTree realTree, P p);

    R visitBooleanTree(BooleanTree booleanTree, P p);

    R visitStringTree(StringTree stringTree, P p);

    R visitConstantTree(ConstantTree constantTree, P p);

    R visitLoadLocalTree(LoadLocalTree loadLocalTree, P p);

    R visitStoreLocalTree(StoreLocalTree storeLocalTree, P p);

    R visitLoadGlobalTree(LoadGlobalTree loadGlobalTree, P p);

    R visitStoreGlobalTree(StoreGlobalTree storeGlobalTree, P p);

    R visitBinaryOperationTree(BinaryOperationTree operationTree, P p);

    R visitUnaryOperationTree(UnaryOperationTree operationTree, P p);

    R visitThisTree(ThisTree thisTree, P p);

    R visitEqualsTree(EqualsTree equalsTree, P p);

    R visitCallTree(CallTree callTree, P p);

    R visitCallSuperTree(CallSuperTree callSuperTree, P p);

    R visitArgumentTree(ArgumentTree argumentTree, P p);

    R visitGetTypeTree(GetTypeTree getTypeTree, P p);

    R visitThrowTree(ThrowTree throwTree, P p);

    R visitArrayTree(ArrayTree arrayTree, P p);

    R visitDictionaryTree(DictionaryTree dictionaryTree, P p);

    R visitRangeTree(RangeTree rangeTree, P p);

    R visitNewLineTree(NewLineTree newLineTree, P p);

    R visitLoadMemberFastTree(LoadMemberFastTree loadTree, P p);

    R visitStoreMemberFastTree(StoreMemberFastTree storeTree, P p);

    R visitLoadMemberTree(LoadMemberTree loadTree, P p);

    R visitStoreMemberTree(StoreMemberTree storeTree, P p);

    R visitAccessUnknownFastTree(AccessUnknownFastTree accessTre, P p);

    R visitLoadStaticTree(LoadStaticTree loadTree, P p);

    R visitStoreStaticTree(StoreStaticTree storeTree, P p);

    R visitWriteContainerTree(WriteContainerTree writeTree, P p);

    R visitReadContainerTree(ReadContainerTree readTree, P p);

    R visitLoadAbstractImplTree(LoadAbstractImplTree loadTree, P p);

    R visitJavaCodeTree(JavaCodeTree javaCodeTree, P p);

}
