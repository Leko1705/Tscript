package com.tscript.runtime.jit;

import com.tscript.runtime.jit.BytecodeParser.*;

public class LoopUnoller extends ParentDelegationPhase<Void> {

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Tree parent) {

        if (forLoopTree.iterable instanceof RangeTree r){

            if (r.from instanceof IntegerTree f
                    && r.to instanceof IntegerTree t
                    && t.value - f.value <= 100){
                SequenceTree unrolled = new SequenceTree();
                for (int i = f.value; i < t.value; i++) {
                    unrolled.children.add(new StoreLocalTree(forLoopTree.address, new IntegerTree(i)));
                    Tree copy = TreeCopier.copy(forLoopTree.body);
                    unrolled.children.add(copy);
                }

                parent.replace(forLoopTree, unrolled);
                optimizationPerformed();
            }
        }

        return null;
    }

}
