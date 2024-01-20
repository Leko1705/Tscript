package tscriptc.tools;

public class CompilerProvider {

    private CompilerProvider(){}

    public static Compiler getDefaultTscriptCompiler(){
        return new TscriptCompiler();
    }

    public static Compiler getCompiler(Language language){
        try {
            Class<? extends Compiler> clazz = language.clazz;
            return clazz.getConstructor().newInstance();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
