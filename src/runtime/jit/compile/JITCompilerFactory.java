package runtime.jit.compile;

public class JITCompilerFactory {

    private static NoJITCompiler noJITCompiler;

    public static JITCompiler getNoJITCompiler() {
        if (noJITCompiler == null) {
            noJITCompiler = new NoJITCompiler();
        }
        return noJITCompiler;
    }

    public static JITCompiler createNewDefaultJITCompiler() {
        return new MethodJIT();
        // return getNoJITCompiler();
    }

}
