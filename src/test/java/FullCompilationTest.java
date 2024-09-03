import com.tscript.tscriptc.tools.Compiler;
import com.tscript.tscriptc.tools.ToolFactory;
import com.tscript.tscriptc.utils.CompileException;
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
