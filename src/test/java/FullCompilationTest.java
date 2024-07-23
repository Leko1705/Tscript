import com.tscript.tscriptc.log.Logger;
import com.tscript.tscriptc.log.StdLogger;
import com.tscript.tscriptc.tools.Compiler;
import com.tscript.tscriptc.tools.CompilerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FullCompilationTest {

    @Test
    public void test(){
        testValidCode("src/test/resources/test.tscript", "src/test/resources/out/test.tscriptc");
    }

    private void testValidCode(String inPath,  String outPath){
        try {
            InputStream in = new FileInputStream(inPath);
            OutputStream out = new FileOutputStream(outPath);

            Compiler compiler = CompilerProvider.getDefaultTscriptCompiler();
            int exitCode = compiler.run(in, out, StdLogger.getLogger(), null);

            Assertions.assertEquals(0, exitCode);
        }
        catch (Exception e){
            Assertions.fail(e);
        }
    }

    private void testInvalidCode(String inPath,  String outPath){
        try {
            InputStream in = new FileInputStream(inPath);
            OutputStream out = new FileOutputStream(outPath);

            Compiler compiler = CompilerProvider.getDefaultTscriptCompiler();
            int exitCode = compiler.run(in, out, StdLogger.getLogger(), null);

            Assertions.assertNotEquals(0, exitCode);
        }
        catch (Exception e){
            Assertions.fail(e);
        }
    }

}
