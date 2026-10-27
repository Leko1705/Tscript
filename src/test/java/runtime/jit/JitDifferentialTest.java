package runtime.jit;

import com.tscript.compiler.tools.ToolFactory;
import com.tscript.runtime.VMFactory;
import com.tscript.runtime.core.TscriptVM;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Runs every program of src/test/resources/jit with the JIT disabled and enabled and checks
 * that both runs produce exactly the same output (stdout, error messages and exit code).
 * The JIT is run with default thresholds and with very low thresholds (so that nearly
 * everything gets traced, compiled and deoptimized).
 */
@Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class JitDifferentialTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/jit");

    static Stream<String> programs() throws IOException {
        try (Stream<Path> files = Files.list(PROGRAMS)) {
            return files.map(p -> p.getFileName().toString())
                    .filter(name -> name.endsWith(".tscript"))
                    .map(name -> name.substring(0, name.length() - ".tscript".length()))
                    .sorted()
                    .toList()
                    .stream();
        }
    }

    @ParameterizedTest
    @MethodSource("programs")
    public void defaultThresholds(String program) throws Exception {
        compareWithInterpreter(program, null, null);
    }

    @ParameterizedTest
    @MethodSource("programs")
    public void lowThresholds(String program) throws Exception {
        compareWithInterpreter(program, "2", "5");
    }

    private void compareWithInterpreter(String program, String threshold, String threshold2) throws Exception {
        File root = compile(program);
        String expected = run(root, program, false, null, null);
        String actual = run(root, program, true, threshold, threshold2);
        Assertions.assertEquals(expected, actual, "output of " + program + " differs with the JIT enabled");
    }

    private File compile(String program) throws Exception {
        File root = Files.createTempDirectory("tscript-jit-test").toFile();
        root.deleteOnExit();
        File out = new File(root, program + ".tscriptc");
        try (InputStream in = new FileInputStream(PROGRAMS.resolve(program + ".tscript").toFile());
             OutputStream os = new FileOutputStream(out)) {
            ToolFactory.createDefaultTscriptCompiler().run(in, os, null);
        }
        out.deleteOnExit();
        return root;
    }

    private String run(File root, String program, boolean jit, String threshold, String threshold2) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;
        setProperty("tscript.jit.threshold", threshold);
        setProperty("tscript.jit.threshold2", threshold2);
        try {
            System.setErr(stream);
            System.setOut(stream);
            TscriptVM vm = VMFactory.newRunnableTscriptVM(root, stream, stream);
            vm.setJitEnabled(jit);
            int exitCode = vm.execute(program);
            stream.println("exit code: " + exitCode);
        }
        finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
            setProperty("tscript.jit.threshold", null);
            setProperty("tscript.jit.threshold2", null);
        }
        String output = buffer.toString(StandardCharsets.UTF_8);
        Assertions.assertFalse(output.contains("An Internal Error occurred"), "internal error in " + program + ":\n" + output);
        return output;
    }

    private static void setProperty(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }
}
