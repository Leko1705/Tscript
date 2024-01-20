package tscriptc.tools;

import tscriptc.analysis.*;
import tscriptc.generation.Compiled;
import tscriptc.generation.Generator;
import tscriptc.log.Logger;
import tscriptc.log.StdLogger;
import tscriptc.parse.*;
import tscriptc.tree.RootTree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

class TscriptCompiler implements Compiler {

    private final List<Checker<?, ?>> checkers =
            List.of(new DefinitionChecker(), new EscapeChecker(), new TypeChecker());

    private Compiled compiled;

    public TscriptCompiler(){}

    @Override
    public String getName() {
        return "tscriptc";
    }

    @Override
    public int run(InputStream in, OutputStream out, Logger logger, String... args) {

        compiled = compile0(in, out, logger, args);
        if (compiled == null) return -1;

        try {
            compiled.write(out);
        }catch (Exception ignored){}

        return 0;
    }

    @Override
    public int dis(InputStream in, OutputStream out, Logger logger, String... args) {

        if (compiled == null) {
            compiled = compile0(in, out, logger, args);
            if (compiled == null)
                return -1;
        }

        try {
            compiled.writeReadable(out);
        }catch (Exception ignored){}

        return 1;
    }

    public Compiled compile0(InputStream in, OutputStream out, Logger logger, String... args) {
        Objects.requireNonNull(in, "InputStream must not be null");

        if (logger == null)
            logger = StdLogger.getLogger();

        RootTree ast = parse(in, logger);
        if (!analyze(ast, logger)) return null;


        Generator generator = new Generator();
        ast.accept(generator);
        return generator.getCompiled();
    }

    private RootTree parse(InputStream in, Logger logger){
        UnicodeReader reader = new UnicodeReader(in);
        Lexer lexer = new Scanner(reader, logger);
        Parser parser = new TscriptParser(lexer, logger);
        return parser.parseProgram();
    }

    private boolean analyze(RootTree rootTree, Logger logger){
        for (Checker<?, ?> checker : checkers)
            if (!checker.check(rootTree, logger))
                return false;
        return true;
    }

}
