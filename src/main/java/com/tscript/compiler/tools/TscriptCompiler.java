package com.tscript.compiler.tools;

import com.tscript.compiler.impl.analyze.*;
import com.tscript.compiler.impl.generation.Generator;
import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.impl.generation.target.Target;
import com.tscript.compiler.impl.generation.target.TscriptBytecode;
import com.tscript.compiler.impl.parse.TscriptParser;
import com.tscript.compiler.impl.utils.DottetClassNameFormatter;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.utils.CompileException;
import com.tscript.compiler.source.utils.InternalToolException;

import java.io.InputStream;
import java.io.OutputStream;

public class TscriptCompiler implements Compiler {

    @Override
    public void run(InputStream in, OutputStream out, String[] args) {

        try {
            TscriptParser parser = TscriptParser.getDefaultSetup(in);
            TCTree tree = parser.parseProgram();

            check(tree);

            CompiledFile lower = Generator.generate(tree);
            Target target = new TscriptBytecode(out);
            target.write(lower);
        }
        catch (CompileException e){
            // rethrow the exception in order to hide
            // core compiler logic
            throw new CompileException(e.message, e.location, e.phase);
        }
        catch (Exception e) {
            throw new InternalToolException(e);
        }

    }

    private static void check(TCTree tree){
        PostSyntaxChecker.check(tree);
        ScopeChecker.check(tree);
        TypeChecker.check(tree);
        SymbolResolver.resolve(tree);
        HierarchyResolver.resolve(tree, new DottetClassNameFormatter());
        UsageApplier.apply(tree);
    }

}
