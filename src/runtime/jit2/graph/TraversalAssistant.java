package runtime.jit2.graph;

import java.util.HashSet;
import java.util.Set;

public class TraversalAssistant {

    private final Set<CFGLink> visitedLinks = new HashSet<>();
    private final Set<CFGNode> visitedNodes = new HashSet<>();

    public boolean hasBeenVisited(CFGLink link) {
        if (visitedLinks.contains(link)) return true;
        visitedLinks.add(link);
        return false;
    }

    public void markVisited(CFGLink link) {
        visitedLinks.add(link);
    }


    public boolean hasBeenVisited(CFGNode node) {
        if (visitedNodes.contains(node)) return true;
        visitedNodes.add(node);
        return false;
    }

    public void markVisited(CFGNode link) {
        visitedNodes.add(link);
    }

}
