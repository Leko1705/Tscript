import runtime.core.TscriptVM;
import tscriptc.log.StdLogger;
import tscriptc.tools.Compiler;
import tscriptc.tools.CompilerProvider;

import java.io.*;

public class Main {

    public static void main(String[] args) {

        String fileName = "test";

        try (InputStream in = new FileInputStream(fileName + ".tscript")){
            Compiler compiler = CompilerProvider.getDefaultTscriptCompiler();

            OutputStream out = new FileOutputStream(fileName + ".tscriptc");
            compiler.run(in, out, StdLogger.getLogger());

            out = new FileOutputStream(fileName + ".tscripti");
            compiler.dis(in, out, StdLogger.getLogger());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long start = System.currentTimeMillis();
        int exitValue = TscriptVM.run(new File(fileName + ".tscriptc"), System.out, System.err);
        long end = System.currentTimeMillis();
        System.out.println("exec time: " + (end - start) + "ms");
        System.exit(exitValue);
    }

}