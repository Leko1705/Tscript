package com.tscript;

import com.tscript.tscriptc.log.Logger;
import com.tscript.tscriptc.tools.Compiler;
import com.tscript.tscriptc.tools.CompilerProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        InputStream in = new FileInputStream("src/main/resources/test.tscript");
        OutputStream out = new FileOutputStream("src/main/resources/out/test.tscriptc");

        Compiler compiler = CompilerProvider.getDefaultTscriptCompiler();
        int exitCode = compiler.run(in, out, Logger.getStandardLogger(), null);

        System.exit(exitCode);
    }
}