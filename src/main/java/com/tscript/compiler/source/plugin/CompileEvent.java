package com.tscript.compiler.source.plugin;

import com.tscript.compiler.source.tree.Tree;
import com.tscript.compiler.impl.utils.Phase;

public interface CompileEvent {

    Tree getTree();

    Phase getPhase();

}
