package com.tscript.tscriptc.tree;

import java.util.List;

public interface RootTree extends Tree {

    List<? extends ModuleTree> getModules();

}
