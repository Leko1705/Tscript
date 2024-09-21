import com.tscript.runtime.core.TscriptVM;
import com.tscript.tscriptc.tools.*;
import com.tscript.tscriptc.tools.Compiler;
import com.tscript.tscriptc.utils.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

public class FullCompilationTest {

    private static final String IN_FILE = "src/test/resources/test.tscript";
    private static final String OUT_FILE_C = "src/test/resources/out/test.tscriptc";
    private static final String OUT_FILE_I = "src/test/resources/out/test.tscripti";

    private static final String ROOT_PATH = "src/test/resources/out/";
    private static final String BOOT_MODULE = "test";

    public static void main(String[] args) {
        new FullCompilationTest().test();
    }

    @Test
    public void test(){
        runTool(ToolFactory.createDefaultTscriptCompiler(),
                "src/test/resources/std.tscript",
                "src/test/resources/out/std.tscriptc");

        runTool(ToolFactory.createDefaultTscriptCompiler(),
                IN_FILE,
                OUT_FILE_C);

        runTool(new TscriptBytecodeInspector(),
                IN_FILE,
                OUT_FILE_I);

        TscriptVM vm = TscriptVM.runnableInstance(new File(ROOT_PATH), System.out, System.err);
        int exitCode = vm.execute(BOOT_MODULE);
        Assertions.assertEquals(0, exitCode);
    }

    private void runTool(Tool tool, String inPath, String outPath, String... args){
        try {
            InputStream in = new FileInputStream(inPath);
            OutputStream out = new FileOutputStream(outPath);

            try {
                tool.run(in, out, args);
            }
            catch (CompileException e) {
                Assertions.fail(e);
            }
        }
        catch (Exception e){
            Assertions.fail(e);
        }

    }

    private void testInvalidCode(String inPath,  String outPath){
        try {
            InputStream in = new FileInputStream(inPath);
            OutputStream out = new FileOutputStream(outPath);

            Compiler compiler = ToolFactory.createDefaultTscriptCompiler();
            try {
                compiler.run(in, out, null);
            }
            catch (CompileException e) {
                Assertions.fail(e);
            }
        }
        catch (Exception e){
            Assertions.fail(e);
        }
    }

}
