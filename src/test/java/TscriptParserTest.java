import com.tscript.tscriptc.log.Logger;
import com.tscript.tscriptc.log.VoidLogger;
import com.tscript.tscriptc.parse.Parser;
import com.tscript.tscriptc.parse.TscriptParser;
import com.tscript.tscriptc.parse.TscriptScanner;
import com.tscript.tscriptc.parse.UnicodeReader;
import com.tscript.tscriptc.transpile.Transpiler;
import com.tscript.tscriptc.transpile.TscriptTranspiler;
import com.tscript.tscriptc.tree.RootTree;
import com.tscript.tscriptc.utils.Diagnostics;
import com.tscript.tscriptc.utils.TreeMaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

public class TscriptParserTest {

    @Test
    public void test(){
        check("x = 1 << 2;");
    }

    private void check(String input){
        checkCallBack(input, parser -> {
            RootTree root = parser.parseProgram();
            Transpiler transpiler = new TscriptTranspiler(false);
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                transpiler.transpile(root, out);
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

        TscriptScanner scanner = new TscriptScanner(unicodeReader, new VoidLogger());
        Parser parser = new TscriptParser(scanner, new Logger() {
            @Override
            public void error(Diagnostics.Error error) {
                Assertions.fail(error.getMessage());
            }

            @Override
            public void warning(Diagnostics.Warning warning) {

            }
        }, new TreeMaker());

        testOp.accept(parser);
    }

}
