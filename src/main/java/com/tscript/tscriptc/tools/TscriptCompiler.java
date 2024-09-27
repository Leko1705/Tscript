package com.tscript.tscriptc.tools;

import com.tscript.tscriptc.analyze.scoping.Scope;
import com.tscript.tscriptc.analyze2.PostSyntaxChecker;
import com.tscript.tscriptc.analyze2.ScopeChecker;
import com.tscript.tscriptc.analyze2.TypeChecker;
import com.tscript.tscriptc.generation.generators.Generator;
import com.tscript.tscriptc.generation.compiled.CompiledFile;
import com.tscript.tscriptc.generation.target.Target;
import com.tscript.tscriptc.generation.target.TscriptBytecode;
import com.tscript.tscriptc.parse.Parser;
import com.tscript.tscriptc.parse.TscriptParser;
import com.tscript.tscriptc.tree.Tree;
import com.tscript.tscriptc.utils.CompileException;
import com.tscript.tscriptc.utils.InternalToolException;

import java.io.InputStream;
import java.io.OutputStream;

public class TscriptCompiler implements Compiler {

    @Override
    public void run(InputStream in, OutputStream out, String[] args) {

        try {
            Parser parser = TscriptParser.getDefaultSetup(in);
            Tree tree = parser.parseProgram();

            Scope scope = check(tree);
            if (true)return;

            CompiledFile lower = Generator.generate(tree, scope);
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

    private static Scope check(Tree tree){
        PostSyntaxChecker.check(tree);
        DefinitionChecker.check(tree);
        ScopeChecker.check(tree);
        TypeChecker.check(tree);
        return null;
    }

}
