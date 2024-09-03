package com.tscript.tscriptc.tools;

import com.tscript.tscriptc.parse.Parser;
import com.tscript.tscriptc.parse.TscriptParser;
import com.tscript.tscriptc.tree.Tree;

import java.io.InputStream;
import java.io.OutputStream;

public class TscriptCompiler implements Compiler {

    @Override
    public void run(InputStream in, OutputStream out, String[] args) {
        Parser parser = TscriptParser.getDefaultSetup(in);
        Tree tree = parser.parseProgram();
    }


}
