package tscriptc.analysis;

import tscriptc.log.Logger;
import tscriptc.tree.RootTree;
import tscriptc.util.Diagnostics;
import tscriptc.util.TreeScanner;
import tscriptc.util.TreeVisitor;

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
