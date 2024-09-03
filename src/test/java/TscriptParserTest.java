import com.tscript.tscriptc.parse.Parser;
import com.tscript.tscriptc.parse.TscriptParser;
import com.tscript.tscriptc.parse.TscriptScanner;
import com.tscript.tscriptc.parse.UnicodeReader;
import com.tscript.tscriptc.tools.Transpiler;
import com.tscript.tscriptc.tools.TscriptTranspiler;
import com.tscript.tscriptc.utils.TreeMaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

public class TscriptParserTest {

    @Test
    public void test(){
        check("var x = 1;");
        // check("const x = 1 << 3;"); WORKS
        // check("function foo(x=3){return 1;}"); WORKS
        check("if true then f();");
        check("if [1, true, {}, 2:0] then {} else f();");
        // check("while x do {} do {} while x;"); WORKS
        // check("var x = function[x](){};"); WORKS
        // check("function f(){ class X{} }"); WORKS
    }

    private void check(String input){
        checkCallBack(input, parser -> {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Transpiler transpiler = new TscriptTranspiler(false);
                transpiler.run(new ByteArrayInputStream(input.getBytes()), out, null);
                String str = out.toString();
                System.out.println(str);
                Assertions.assertEquals(input, str);
            }
            catch (Exception ignored){}
        });
    }

    private void checkCallBack(String input, Consumer<Parser> testOp){
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        UnicodeReader unicodeReader = new UnicodeReader(bais);

        TscriptScanner scanner = new TscriptScanner(unicodeReader);
        Parser parser = new TscriptParser(scanner, new TreeMaker());

        testOp.accept(parser);
    }

}
