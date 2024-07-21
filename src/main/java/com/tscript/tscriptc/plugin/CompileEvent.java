package com.tscript.tscriptc.plugin;

import com.tscript.tscriptc.tree.Tree;
import com.tscript.tscriptc.utils.Phase;

public interface CompileEvent {

    Tree getTree();

    Phase getPhase();

}
