package compiler.impl.analyze;

import com.tscript.compiler.source.utils.CompileException;
import com.tscript.compiler.tools.Tool;
import com.tscript.compiler.tools.ToolFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class CheckTest {

    public static void main(String[] args) {
        new CheckTest().test();
    }

    @Test
    public void test() {
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

        check("""
                class X { }
                class Y: X { }
                """, true);
        check("""
                class X: Y { }
                class Y: X { }
                """, false);
        check("""
                class X {
                public: var x;
                }
                class Y: X {
                    public:
                    function f(){
                        print(x);
                    }
                }
                """, true);
        check("""
                class X {
                public: var x;
                }
                class Y: X {
                    public:
                    function f(){
                        print(z);
                    }
                }
                """, false);
        check("""
                class X {
                private: var x;
                }
                class Y: X {
                    public:
                    function f(){
                        print(x);
                    }
                }
                """, false);

        check("""
                class X {
                public: var x;
                }
                class Y: X {
                    public:
                    function f(){
                        print(super.x);
                    }
                }
                """, true);
        check("""
                class X {
                private: var x;
                }
                class Y: X {
                    public:
                    function f(){
                        print(super.x);
                    }
                }
                """, false);

        check("""
                class X { }
                class Y: X { }
                class Z: Y { }
                """, true);

        check("""
                class X: Z { }
                class Y: X { }
                class Z: Y { }
                """, false);
        check("""
                class X {
                public: var x;
                }
                class Y: X {
                }
                class Z: Y {
                public:
                    function f(){
                        print(x);
                    }
                }
                """, true);
        check("""
                class X {
                private: var x;
                }
                class Y: X {
                }
                class Z: Y {
                public:
                    function f(){
                        print(x);
                    }
                }
                """, false);
        check("""
                class X {
                public: var x;
                }
                class Y: X {
                }
                class Z: Y {
                public:
                    function f(){
                        print(z);
                    }
                }
                """, false);
        check("""
                class X {
                public: var x;
                }
                class Y: X {
                }
                class Z: Y {
                public:
                    function f(){
                        print(super.x);
                    }
                }
                """, true);
        check("""
                class X {
                private: var x;
                }
                class Y: X {
                }
                class Z: Y {
                public:
                    function f(){
                        print(super.x);
                    }
                }
                """, false);
        check("""
                class X {
                public: var x;
                }
                class Y: X {
                }
                class Z: Y {
                public:
                    function f(){
                        print(super.z);
                    }
                }
                """, false);
        check("""
                class X {
                    public:
                    var x;
                    function f(){
                        print(this.x);
                    }
                }
                """, true);
        check("""
                class X {
                    public:
                    var x;
                    function f(){
                        print(this.z);
                    }
                }
                """, false);
        check("""
                class X {
                    private:
                    var x;
                    function f(){
                        print(this.z);
                    }
                }
                """, false);
        check("""
                class X {
                public: var x;
                }
                class Y: X {
                    public:
                    function f(){
                        print(this.x);
                    }
                }
                """, false);
        check("""
                class X {
                    public:
                    constructor(): super(2){}
                }
                """, true);
        check("""
                class X {
                    public:
                    constructor(x): super(x){}
                }
                """, true);
        check("""
                class X {
                    public:
                    constructor(): super(this){}
                }
                """, false);
        check("""
                class X {
                    public:
                    constructor(): super(super.x){}
                }
                """, false);
        check("""
                class X {
                    public:
                    var x;
                    constructor(): super(x){}
                }
                """, false);
        check("""
                class X {
                public:
                    static var x;
                    static function f(){
                        print(x);
                    }
                }
                """, true);
        check("""
                class X {
                public:
                    var x;
                    static function f(){
                        print(x);
                    }
                }
                """, false);
        check("""
                class X {
                public:
                    static var x;
                    function f(){
                        print(x);
                    }
                }
                """, true);
    }

    private static void check(String code, boolean shouldPass){
        Tool compiler = ToolFactory.createDefaultTscriptCompiler();
        try {
            compiler.run(new ByteArrayInputStream(code.getBytes()), System.out, null);
        }
        catch (CompileException e) {
            if (shouldPass)
                throw new AssertionError("\n"+code, e);
            return;
        }
        if (!shouldPass)
            throw new AssertionError("should fail:\n" + code);
    }

}
