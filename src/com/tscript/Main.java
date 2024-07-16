package com.tscript;

import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.debug.Debugger;
import com.tscript.tscriptc.log.StdLogger;
import com.tscript.tscriptc.tools.Compiler;
import com.tscript.tscriptc.tools.CompilerProvider;

import java.io.*;

public class Main {

    public static void main(String[] args) {

        String fileName = "test";

        compileAndDis(fileName);
        exec(fileName);
    }

    private static void compile(String path, String... args){
        try (InputStream in = new FileInputStream(path + ".tscript")){
            Compiler compiler = CompilerProvider.getDefaultTscriptCompiler();

            OutputStream out = new FileOutputStream(path + ".com.tscript.tscriptc");
            compiler.run(in, out, StdLogger.getLogger(), args);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compileAndDis(String path, String... args){
        try (InputStream in = new FileInputStream(path + ".tscript")){
            Compiler compiler = CompilerProvider.getDefaultTscriptCompiler();

            OutputStream out = new FileOutputStream(path + ".com.tscript.tscriptc");
            compiler.run(in, out, StdLogger.getLogger(), args);

            out = new FileOutputStream(path + ".tscripti");
            compiler.dis(in, out, StdLogger.getLogger(), args);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void exec(String path){
        long start = System.currentTimeMillis();
        int exitValue = TscriptVM.run(new File(path + ".com.tscript.tscriptc"), System.out, System.err, Debugger.getDefaultDebugger());
        long end = System.currentTimeMillis();
        System.out.println("exec time: " + (end - start) + "ms");
        System.exit(exitValue);
    }

}