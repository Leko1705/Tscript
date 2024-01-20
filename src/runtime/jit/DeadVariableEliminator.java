package runtime.jit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import runtime.jit.BytecodeParser.*;

public class DeadVariableEliminator extends ParentDelegationPhase<Void> {

    private final Map<Integer, List<RemovableCandidate>> unused = new HashMap<>();

    private void removeUnused(){
        for (List<RemovableCandidate> indices : unused.values()) {
            for (RemovableCandidate candidate : indices) {
                candidate.parent.remove(candidate.removable);
                optimizationPerformed();
            }
            indices.clear();
        }
    }

    private record RemovableCandidate(Tree removable, Tree parent){
    }

    @Override
    public Void visitRootTree(RootTree rootTree, Tree parent) {
        super.visitRootTree(rootTree, rootTree);
        removeUnused();
        return null;
    }

    @Override
    public Void visitStoreLocalTree(StoreLocalTree storeLocalTree, Tree parent) {
        scan(storeLocalTree.child, storeLocalTree);

        if (storeLocalTree.child instanceof LoadLocalTree l){
            if (storeLocalTree.address == l.address()){
                parent.remove(storeLocalTree);
                optimizationPerformed();
            }
        }

        int address = storeLocalTree.address;
        if (!unused.containsKey(address))
            unused.put(address, new LinkedList<>());
        unused.get(address).add(new RemovableCandidate(storeLocalTree, parent));

        return null;
    }

    @Override
    public Void visitLoadLocalTree(LoadLocalTree loadLocalTree, Tree parent) {
        int address = loadLocalTree.address();
        if (!unused.containsKey(address))
            unused.put(address, new LinkedList<>());
        unused.get(loadLocalTree.address()).clear();
        return null;
    }

    @Override
    public Void visitStoreGlobalTree(StoreGlobalTree storeGlobalTree, Tree parent) {
        scan(storeGlobalTree.child, storeGlobalTree);

        if (storeGlobalTree.child instanceof LoadGlobalTree l){
            if (storeGlobalTree.address == l.address()){
                parent.remove(storeGlobalTree);
                optimizationPerformed();
            }
        }

        return null;
    }

}
