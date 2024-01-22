package runtime.jit;

import runtime.jit.BytecodeParser.*;

import java.util.Deque;
import java.util.LinkedList;

public class UnsafeReducer extends ParentDelegationPhase<Void> {

    private record LastLine(NewLineTree lineTree, Tree parent){
    }

    private final Deque<LastLine> lastLineStack = new LinkedList<>();

    private LastLine lastLine = null;

    private boolean unsafeDetected = false;


    private Void handleUnsafe(Tree tree){
        scan(tree, tree);
        lastLine = null; // hold the line alive
        unsafeDetected = true;
        return null;
    }

    private void enterScope(){
        lastLineStack.push(lastLine);
        lastLine = null;
    }

    private void leaveScope(){
        lastLine = lastLineStack.pop();
    }

    private void removeLastLine(){
        lastLine.parent.remove(lastLine.lineTree);
        lastLine = null;
        optimizationPerformed();
    }

    @Override
    public Void visitRootTree(RootTree rootTree, Tree parent) {
        super.visitRootTree(rootTree, parent);
        if(lastLine != null)
            removeLastLine();
        return null;
    }

    @Override
    public Void visitNewLineTree(NewLineTree newLineTree, Tree parent) {
        if (lastLine != null) {
            removeLastLine();
            return null;
        }
        lastLine = new LastLine(newLineTree, parent);
        return null;
    }

    @Override
    public Void visitTryCatchTree(TryCatchTree tryCatchTree, Tree parent) {
        boolean outerUnsafeDetected = unsafeDetected;
        unsafeDetected = false;

        enterScope();
        scan(tryCatchTree.tryBody, tryCatchTree);
        leaveScope();

        if (!unsafeDetected){
            parent.replace(tryCatchTree, tryCatchTree.tryBody);
            unsafeDetected = outerUnsafeDetected;
            return null;
        }

        unsafeDetected = outerUnsafeDetected;
        scan(tryCatchTree.catchBody, tryCatchTree);
        return null;
    }

    @Override
    public Void visitIfElseTree(IfElseTree ifElseTree, Tree parent) {
        scan(ifElseTree.condition, ifElseTree);

        enterScope();
        scan(ifElseTree.ifBody, ifElseTree);
        leaveScope();

        enterScope();
        scan(ifElseTree.elseBody, ifElseTree);
        leaveScope();

        return null;
    }

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Tree parent) {
        scan(forLoopTree.iterable, forLoopTree);

        enterScope();
        scan(forLoopTree.body, forLoopTree);
        leaveScope();

        return null;
    }

    @Override
    public Void visitLoadMemberTree(LoadMemberTree loadTree, Tree parent) {
        return handleUnsafe(loadTree.exp);
    }

    @Override
    public Void visitReadContainerTree(ReadContainerTree readTree, Tree parent) {
        return handleUnsafe(readTree);
    }

    @Override
    public Void visitStoreMemberTree(StoreMemberTree storeTree, Tree parent) {
        return handleUnsafe(storeTree);
    }

    @Override
    public Void visitWriteContainerTree(WriteContainerTree writeTree, Tree parent) {
        return handleUnsafe(writeTree);
    }

    @Override
    public Void visitRangeTree(RangeTree rangeTree, Tree parent) {
        scan(rangeTree.to, rangeTree);
        scan(rangeTree.from, rangeTree);
        lastLine = null; // hold the line alive
        unsafeDetected = true;
        return null;
    }

    @Override
    public Void visitThrowTree(ThrowTree throwTree, Tree parent) {
        return handleUnsafe(throwTree);
    }

    @Override
    public Void visitCallTree(CallTree callTree, Tree parent) {
        return handleUnsafe(callTree);
    }

    @Override
    public Void visitLoadAbstractImplTree(LoadAbstractImplTree loadTree, Tree parent) {
        return handleUnsafe(null);
    }

    @Override
    public Void visitConstantTree(ConstantTree constantTree, Tree parent) {
        return handleUnsafe(null);
    }

    @Override
    public Void visitCallSuperTree(CallSuperTree callSuperTree, Tree parent) {
        return handleUnsafe(callSuperTree);
    }

}
