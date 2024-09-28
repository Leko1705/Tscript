package com.tscript.compiler.tools;

import com.tscript.compiler.impl.analyze.PostSyntaxChecker;
import com.tscript.compiler.impl.analyze.ScopeChecker;
import com.tscript.compiler.impl.analyze.TypeChecker;
import com.tscript.compiler.impl.generation.generators.Generator;
import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.impl.generation.target.Target;
import com.tscript.compiler.impl.generation.target.TscriptBytecode;
import com.tscript.compiler.impl.parse.Parser;
import com.tscript.compiler.impl.parse.TscriptParser;
import com.tscript.compiler.source.tree.Tree;
import com.tscript.compiler.source.utils.CompileException;
import com.tscript.compiler.source.utils.InternalToolException;

import java.io.InputStream;
import java.io.OutputStream;

public class TscriptCompiler implements Compiler {

    @Override
    public void run(InputStream in, OutputStream out, String[] args) {

        try {
            Parser parser = TscriptParser.getDefaultSetup(in);
            Tree tree = parser.parseProgram();

            check(tree);
            if (true)return;

            CompiledFile lower = Generator.generate(tree, null);
            Target target = new TscriptBytecode(out);
            target.write(lower);
        }
        catch (CompileException e){
            throw e;
        }
        catch (Exception e) {
            throw new InternalToolException(e);
        }

    }

    private static void check(Tree tree){
        PostSyntaxChecker.check(tree);
        //DefinitionChecker.check(tree);
        ScopeChecker.check(tree);
        TypeChecker.check(tree);
    }

}
