package runtime.jit2.graph;

import runtime.jit2.graph.tree.Tree;

import java.util.ArrayList;
import java.util.List;

public class CFGNode {

    public final List<Tree> trees = new ArrayList<>();

    public CFGLink link;

}
