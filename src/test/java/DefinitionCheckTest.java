import com.tscript.compiler.impl.parse.Parser;
import com.tscript.compiler.impl.parse.TscriptParser;
import com.tscript.compiler.source.tree.Tree;
import com.tscript.compiler.source.utils.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class DefinitionCheckTest {

    @Test
    public void test(){

        // duplicate checks

        check("var x;", true);
        check("var x; var y;", true);
        check("var x; var x;", false);

        check("{ var x; var y; }", true);
        check("{ var x; var x; }", false);

        check("var x; { var y; }", true);
        check("var x; { var x; }", false);
        check("{ var x; } var x;", true);
        check("{ var x; } var y;", true);

        check("var x; function f(){ var x; }", true);
        check("var x; function f(x){ var x; }", false);
        check("var x; function f(x){ }", true);

        check("var x; function f(x, y){}", true);
        check("var x; function f(x, x){}", false);

        check("var x = function(){ var x; };", true);
        check("var x = function[x](){ var x; };", false);
        check("var x = function[y](){ var x; };", true);
        check("var x = function[](x){ var x; };", false);
        check("var x = function[](y){ var x; };", true);


        // constant must be initialized

        check("const x = 2;", true);
        check("const x;", false);

        // check if is defined

        check("var x; print(x);", true);
        check("var x; print(y);", false);

        check("const x = 3; print(x);", true);
        check("const x = 3; print(y);", false);

        check("var x; function f(){ print(x); }", true);

        check("function f(){ function f(){ } }", true);
        check("function f(){ var f; function f(){ } }", false);
        check("function f(){ function f(){ } var f; }", false);

        check("""
                class X {
                    public: function f() {}
                    public: function g() {}
                }
                """, true);
        check("""
                class X {
                    public: function f() {}
                    public: function f() {}
                }
                """, false);
        check("""
                class X {
                    public: var g;
                    public: function f() {}
                }
                """, true);
        check("""
                class X {
                    public: var f;
                    public: function f() {}
                }
                """, false);
        check("""
                class X {
                    public: var f;
                    public: function g() {
                        print(f);
                    }
                }
                """, true);
        check("""
                class X {
                    public: var f;
                    public: function g() {
                        print(y);
                    }
                }
                """, false);
        check("""
                var f;
                class X {
                    public: var f;
                    public: function g() {
                        print(f);
                    }
                }
                """, true);
        check("""
                var f;
                class X {
                    public: var f;
                    public: function g() {
                        print(this.f);
                    }
                }
                """, true);
    }

    private static void check(String code, boolean shouldWork){
        Parser parser = TscriptParser.getDefaultSetup(new ByteArrayInputStream(code.getBytes()));
        Tree tree = parser.parseProgram();
        try {
            //DefinitionChecker.check(tree);
        }
        catch (CompileException ex){
            if (shouldWork)
                Assertions.fail(ex.getMessage());
            return;
        }

        if (!shouldWork)
            Assertions.fail("should fail");
    }

}
