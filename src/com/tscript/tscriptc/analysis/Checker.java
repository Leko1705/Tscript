package com.tscript.tscriptc.analysis;

import com.tscript.tscriptc.log.Logger;
import com.tscript.tscriptc.tree.RootTree;
import com.tscript.tscriptc.util.Diagnostics;
import com.tscript.tscriptc.util.TreeScanner;
import com.tscript.tscriptc.util.TreeVisitor;

import java.util.Objects;

public class Checker<P, R> extends TreeScanner<P, R> {

    private Logger log;

    private boolean success = true;

    public boolean check(RootTree rootTree, Logger logger){
        this.log = logger;
        Objects.requireNonNull(rootTree);
        scan(rootTree, null);
        return success;
    }

    public void report(Diagnostics.Error error){
        log.error(error);
        success = false;
    }

}
