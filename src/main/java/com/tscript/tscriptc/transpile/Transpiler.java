package com.tscript.tscriptc.transpile;

import com.tscript.tscriptc.tree.Tree;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Transforms a given abstract syntax tree into a high level
 * representation. The result is passed to the <code>OutputStream</code>.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface Transpiler {

    /**
     * Transforms the code to the high level representation.
     * @param tree the tree to transform
     * @param out the OutputStream to pass the result to
     * @throws IOException delegate from {@link OutputStream#write(int)}
     */
    void transpile(Tree tree, OutputStream out) throws IOException;

}
