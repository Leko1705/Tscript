package runtime.jit;

import runtime.type.Callable;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Compiler {

    private Compiler(){
    }

    public static Callable compile(String javaClassName, String source){
        try {
            return compile0(javaClassName, source);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static Callable compile0(String javaClassName, String source) throws Exception {
        File root = Files.createTempDirectory("java").toFile();
        File sourceFile = new File(root, javaClassName + ".java");
        sourceFile.getParentFile().mkdirs();
        Files.writeString(sourceFile.toPath(), source);

        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());

        // Load and instantiate compiled class.
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
        Class<?> cls = Class.forName(javaClassName, true, classLoader);
        Object instance = cls.getDeclaredConstructor().newInstance();
        return (Callable) instance;
    }

}
