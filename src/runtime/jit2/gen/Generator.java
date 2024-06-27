package runtime.jit2.gen;

import runtime.jit2.graph.CFGNode;
import runtime.type.Callable;

public interface Generator {

    Callable generate(CFGNode node);

}
