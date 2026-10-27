
# How to configure the Compiler and Virtual Machine

This Tscript implementation is purely written in Java.
Therefore, the compiler and its Runtime could also be used 
from Java code:<p>


```java
import java.io.*;

import com.tscript.compiler.tools.ToolFactory;
import com.tscript.compiler.tools.Compiler;

import com.tscript.runtime.VirtualMachine;
import com.tscript.runtime.VMFactory;

class Main {
    
    // path to source code file
    private static final String SCRIPT_PATH = "code.tscript";
    
    // path to bytecode file
    private static final String BYTECODE_PATH = "code.tscriptc";
    
    // Directory of the root path.
    // The root path is used by the vm as an anchor for searching/loading
    // the bytecode files.
    // There can be multiple Root paths per VM but at least one.
    private static final String ROOT_PATH = "path/to/directory";

    public static void main(String[] args) {
        
        InputStream in = new FileInputStream(SCRIPT_PATH);
        OutputStream out = new FileOutputStream(BYTECODE_PATH);

        Compiler compiler = CompilerProvider.createDefaultTscriptCompiler();
        int exitCode = compiler.run(in, out, null, null);
        if (exitCode != 0){
            // compilation error occurred
            System.exit(exitCode);
        }

        VirtualMachine vm = VMFactory.newRunnableTscriptVM(new File(ROOT_PATH), System.out, System.err);
        exitCode = vm.execute("moduleName"); // see code.tscript
        System.exit(exitCode);
    }
    
}
```