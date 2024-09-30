package compiler.impl;

import com.tscript.compiler.tools.SupportedTool;
import com.tscript.compiler.tools.Tool;
import com.tscript.compiler.tools.ToolFactory;

import java.io.ByteArrayInputStream;

public class FullBytecodeInspectionTest {

    public static void main(String[] args) {
        Tool tool = ToolFactory.loadTool(SupportedTool.TSCRIPT_BC_INSPECTOR);

        String code = """
                function foo() {
                    native function bar;
                    bar();
                    return 1;
                }
                """;

        tool.run(new ByteArrayInputStream(code.getBytes()), System.out, null);
    }

}
