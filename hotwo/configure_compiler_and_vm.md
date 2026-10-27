
# How to configure the Compiler and Virtual Machine

This Tscript implementation is purely written in Java.
Therefore, the compiler and its runtime can also be used
directly from Java code:<p>


```java
import java.io.*;

import com.tscript.compiler.tools.Compiler;
import com.tscript.compiler.tools.ToolFactory;
import com.tscript.compiler.source.utils.CompileException;

import com.tscript.runtime.VMFactory;
import com.tscript.runtime.core.TscriptVM;

class Main {

    // path to source code file
    private static final String SCRIPT_PATH = "code.tscript";

    // Directory of the root path.
    // The root path is used by the vm as an anchor for searching/loading
    // the bytecode files.
    // There can be multiple root paths per VM but at least one.
    private static final String ROOT_PATH = "path/to/directory";

    // path to bytecode file (must be located in a root path)
    private static final String BYTECODE_PATH = ROOT_PATH + "/code.tscriptc";

    public static void main(String[] args) throws IOException {

        try (InputStream in = new FileInputStream(SCRIPT_PATH);
             OutputStream out = new FileOutputStream(BYTECODE_PATH)) {
            Compiler compiler = ToolFactory.createDefaultTscriptCompiler();
            compiler.run(in, out, null);
        }
        catch (CompileException e) {
            // compilation error occurred
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        TscriptVM vm = VMFactory.newRunnableTscriptVM(new File(ROOT_PATH), System.out, System.err);
        int exitCode = vm.execute("moduleName"); // the name given by `module moduleName;` in code.tscript
        System.exit(exitCode);
    }

}
```

### Notes

- `Compiler.run(...)` does not return an exit code. Compilation errors are
  thrown as a `com.tscript.compiler.source.utils.CompileException`.
- The VM looks modules up by their **module name** (the name declared with
  `module <name>;`), not by the file name.
- Besides the compiler, `ToolFactory.loadTool(SupportedTools.TSCRIPT_BC_INSPECTOR)`
  provides a tool that writes the bytecode in a human-readable format, which
  can be useful for debugging.
- A `TscriptVM` can only execute once. All configuration (debugger, JIT, project file,
  ...) must be done before calling `execute`.

### Native functions

Native functions are looked up in the project file of the VM. A VM created
by hand has none, so every `native function` call fails with an error.
To use native functions, parse a project file and attach it to the VM:

```java
ProjectFile projectFile = ProjectFile.parse("path/to/project.tsrt");
vm.setBuildFile(projectFile);
```

In most cases it is easier to run the whole project with
`ProjectFileRunner.runTscriptProject(projectFile)` (see [project setup](project_setup.md)),
which compiles the sources, creates the VM and configures it from the project file.

### JIT compiler

The VM ships with a [just-in-time compiler](../docs/jit_compiler.md), which is
enabled by default. It can be turned off before the VM is started:

```java
vm.setJitEnabled(false); // only use the bytecode interpreter
```
