package com.tscript.runtime.jit;

import com.tscript.runtime.jit.BytecodeParser.*;

public class DeadBlockEliminator extends ParentDelegationPhase<Void> {


    @Override
    public Void visitIfElseTree(IfElseTree ifElseTree, Tree parent) {
        scan(ifElseTree.condition, ifElseTree);
        scan(ifElseTree.ifBody, ifElseTree);
        scan(ifElseTree.elseBody, ifElseTree);

        if (isTrue(ifElseTree.condition)){
            optimizationPerformed();
            parent.replace(ifElseTree, ifElseTree.ifBody);
            return null;
        }
        else if (isFalse(ifElseTree.condition)){
            optimizationPerformed();
            if (ifElseTree.elseBody != null)
                parent.replace(ifElseTree, ifElseTree.elseBody);
            else
                parent.remove(ifElseTree);
            return null;
        }

        if (ifElseTree.elseBody instanceof SequenceTree s){
            if (isEmpty(s)) {
                ifElseTree.elseBody = null;
                optimizationPerformed();
            }
        }

        if (ifElseTree.ifBody instanceof SequenceTree s){
            if (isEmpty(s)){
                if (ifElseTree.elseBody == null) {
                    parent.remove(ifElseTree);
                }
                else {
                    ifElseTree.ifBody = ifElseTree.elseBody;
                    ifElseTree.elseBody = null;
                    ifElseTree.ifTrue = !ifElseTree.ifTrue;
                }
                optimizationPerformed();
            }
        }

        return null;
    }

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Tree parent) {
        scan(forLoopTree.iterable, forLoopTree);
        scan(forLoopTree.body, forLoopTree);

        if (forLoopTree.body instanceof SequenceTree s){
            if (isEmpty(s)){
                parent.remove(forLoopTree);
                optimizationPerformed();
                return null;
            }
        }

        return null;
    }


    @Override
    public Void visitTryCatchTree(TryCatchTree tryCatchTree, Tree parent) {
        scan(tryCatchTree.tryBody, tryCatchTree);
        scan(tryCatchTree.catchBody, tryCatchTree);

        if (tryCatchTree.tryBody instanceof SequenceTree s){
            if (isEmpty(s)){
                parent.remove(tryCatchTree);
                optimizationPerformed();
            }
        }

        return null;
    }

    private boolean isTrue(Tree tree){
        return (tree instanceof BooleanTree b && b.value)
                || (tree instanceof IntegerTree i && i.value != 0)
                || (tree instanceof ArrayTree a && !a.arguments().isEmpty())
                || (tree instanceof DictionaryTree d && !d.arguments.isEmpty());
    }

    private boolean isFalse(Tree tree){
        return (tree instanceof BooleanTree b && !b.value)
                || (tree instanceof IntegerTree i && i.value == 0)
                || (tree instanceof ArrayTree a && a.arguments().isEmpty())
                || (tree instanceof DictionaryTree d && d.arguments.isEmpty());
    }

    private boolean isEmpty(SequenceTree s){
        if (s.children.isEmpty()) return true;
        for (Tree child : s.children)
            if (child instanceof SequenceTree ss)
                if (isEmpty(ss))
                    return true;
        return false;
    }
}
