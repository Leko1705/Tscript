package com.tscript.runtime.jit;

public interface TreeVisitor<R, P> {

    R visitRootTree(BytecodeParser.RootTree rootTree, P p);

    R visitSequenceTree(BytecodeParser.SequenceTree sequenceTree, P p);

    R visitReturnTree(BytecodeParser.ReturnTree returnTree, P p);

    R visitNullTree(BytecodeParser.NullTree nullTree, P p);

    R visitIntegerTree(BytecodeParser.IntegerTree integerTree, P p);

    R visitRealTree(BytecodeParser.RealTree realTree, P p);

    R visitBooleanTree(BytecodeParser.BooleanTree booleanTree, P p);

    R visitStringTree(BytecodeParser.StringTree stringTree, P p);

    R visitConstantTree(BytecodeParser.ConstantTree constantTree, P p);

    R visitLoadLocalTree(BytecodeParser.LoadLocalTree loadLocalTree, P p);

    R visitStoreLocalTree(BytecodeParser.StoreLocalTree storeLocalTree, P p);

    R visitLoadGlobalTree(BytecodeParser.LoadGlobalTree loadGlobalTree, P p);

    R visitStoreGlobalTree(BytecodeParser.StoreGlobalTree storeGlobalTree, P p);

    R visitBinaryOperationTree(BytecodeParser.BinaryOperationTree operationTree, P p);

    R visitUnaryOperationTree(BytecodeParser.UnaryOperationTree operationTree, P p);

    R visitThisTree(BytecodeParser.ThisTree thisTree, P p);

    R visitEqualsTree(BytecodeParser.EqualsTree equalsTree, P p);

    R visitCallTree(BytecodeParser.CallTree callTree, P p);

    R visitCallSuperTree(BytecodeParser.CallSuperTree callSuperTree, P p);

    R visitArgumentTree(BytecodeParser.ArgumentTree argumentTree, P p);

    R visitGetTypeTree(BytecodeParser.GetTypeTree getTypeTree, P p);

    R visitThrowTree(BytecodeParser.ThrowTree throwTree, P p);

    R visitArrayTree(BytecodeParser.ArrayTree arrayTree, P p);

    R visitDictionaryTree(BytecodeParser.DictionaryTree dictionaryTree, P p);

    R visitRangeTree(BytecodeParser.RangeTree rangeTree, P p);

    R visitNewLineTree(BytecodeParser.NewLineTree newLineTree, P p);

    R visitLoadMemberFastTree(BytecodeParser.LoadMemberFastTree loadTree, P p);

    R visitStoreMemberFastTree(BytecodeParser.StoreMemberFastTree storeTree, P p);

    R visitLoadMemberTree(BytecodeParser.LoadMemberTree loadTree, P p);

    R visitStoreMemberTree(BytecodeParser.StoreMemberTree storeTree, P p);

    R visitAccessUnknownFastTree(BytecodeParser.AccessUnknownFastTree accessTre, P p);

    R visitLoadStaticTree(BytecodeParser.LoadStaticTree loadTree, P p);

    R visitStoreStaticTree(BytecodeParser.StoreStaticTree storeTree, P p);

    R visitWriteContainerTree(BytecodeParser.WriteContainerTree writeTree, P p);

    R visitReadContainerTree(BytecodeParser.ReadContainerTree readTree, P p);

    R visitLoadAbstractImplTree(BytecodeParser.LoadAbstractImplTree loadTree, P p);

    R visitTryCatchTree(BytecodeParser.TryCatchTree tryCatchTree, P p);

    R visitForLoopTree(BytecodeParser.ForLoopTree forLoopTree, P p);

    R visitIfElseTree(BytecodeParser.IfElseTree ifElseTree, P p);

    R visitJavaCodeTree(BytecodeParser.JavaCodeTree javaCodeTree, P p);

}
