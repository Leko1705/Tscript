package runtime.jit2.graph.creation;

import runtime.jit2.graph.BranchedLink;
import runtime.jit2.graph.CFGNode;
import runtime.jit2.graph.SimpleLink;
import runtime.jit2.graph.tree.Tree;

import java.util.*;

public class CFGBuilder {


    private final CFGNode root = new CFGNode();
    private CFGNode current = root;


    private final Map<Integer, CFGNode> knownNodes = new HashMap<>();
    private final Map<Integer, CFGNode> futureNodes = new HashMap<>();


    public void extend(int index, Tree tree){

        if (futureNodes.containsKey(index)){
            current = futureNodes.remove(index);
            knownNodes.put(index, current);
        }

        current.trees.add(tree);
        knownNodes.put(index, current);
    }

    public void branch(int index, Tree condition, boolean branchIfFalse, int target){

        BranchedLink link = new BranchedLink();
        link.condition = condition;
        link.branchIfFalse = branchIfFalse;

        if (index == target){
            branchSelf(index, link);
            return;
        }

        CFGNode targetCandidate = knownNodes.get(target);
        if (targetCandidate != null){
            branchBack(index, link, targetCandidate);
        }
        else {
            branchForward(index, link);
        }
    }
    private void branchSelf(int index, BranchedLink link){
        CFGNode next = new CFGNode();
        current.link = new SimpleLink(next, false);



    }

    private void branchBack(int index, BranchedLink link, CFGNode targetNode){
        if (link.branchIfFalse){
            link.T = targetNode;
5
        }
    }

    private void branchForward(int index, BranchedLink link){

    }


    public CFGNode create(){
        return root;
    }

}
