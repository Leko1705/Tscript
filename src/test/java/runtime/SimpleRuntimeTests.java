package runtime;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.core.TscriptVM;
import org.junit.jupiter.api.Assertions;

import java.io.File;

public class SimpleRuntimeTests {

    public static void main(String[] args) {
        File mainFile;
        try {

            /*
            asm für source-code test.tscriptc:
                import test1;
                var foo = test1.func;
                foo("hello world!");

            asm für source-code test1.tscriptc:
                var func = print;
             */

            mainFile = FileBuilder.newBuilder("path_to_exec_file", "test")
                    .addGlobal("test1", false)
                    .addGlobal("foo", true)
                    .pool()
                            .addUTF8("test1")
                            .addUTF8("func")
                            .addString("hello world!")
                            .complete()
                    .newFunction("__main__")
                            .withLocals(1)
                            .withStackSize(2)
                            .appendInstruction(Opcode.IMPORT, 0, 0)
                            .appendInstruction(Opcode.STORE_GLOBAL, 0)
                            .appendInstruction(Opcode.LOAD_GLOBAL, 0)
                            .appendInstruction(Opcode.LOAD_EXTERNAL, 0, 1)
                            .appendInstruction(Opcode.STORE_GLOBAL, 1)
                            .appendInstruction(Opcode.LOAD_CONST, 0, 2)
                            .appendInstruction(Opcode.LOAD_GLOBAL, 1)
                            .appendInstruction(Opcode.CALL_INPLACE, 1)
                            .appendInstruction(Opcode.POP)
                            .appendInstruction(Opcode.PUSH_NULL)
                            .appendInstruction(Opcode.RETURN)
                            .register()
                    .write();

            FileBuilder.newBuilder("path_to_some_other_file_to_import", "test1")
                    .addGlobal("func", false)
                    .pool()
                            .addUTF8("print")
                            .complete()
                    .newFunction("__main__")
                            .withLocals(0)
                            .withStackSize(2)
                            .appendInstruction(Opcode.LOAD_NATIVE, 0, 0)
                            .appendInstruction(Opcode.STORE_GLOBAL, 0)
                            .appendInstruction(Opcode.PUSH_NULL)
                            .appendInstruction(Opcode.RETURN)
                            .register()
                    .write();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e);
            return;
        }

        File rootPath = new File(mainFile.getParent());
        TscriptVM vm = TscriptVM.runnableInstance(rootPath, System.out, System.err);
        int exitCode = vm.execute("test");
        Assertions.assertEquals(0, exitCode);
    }


}
