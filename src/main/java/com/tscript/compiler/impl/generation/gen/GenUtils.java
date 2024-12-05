package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.utils.Symbol;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.ArgumentTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenUtils {

    public static void genCall(Context context,
                               List<? extends TCTree.TCArgumentTree> args,
                               Runnable genCalled,
                               FunctionGenerator gen){
        boolean isMapped = isMappedCall(args);

        if (isMapped) {
            gen.func.getInstructions().addAll(genMappedArgs(context, args, gen));
            genCalled.run();
            gen.func.getInstructions().add(new CallMapped(args.size()));
            gen.stackShrinks(args.size());
        }
        else {
            genInplaceArgs(args, gen);
            genCalled.run();
            gen.func.getInstructions().add(new CallInplace(args.size()));
            gen.stackShrinks(args.size());
        }

    }

    public static List<Instruction> genArgFetch(Context context,
                                                List<? extends TCTree.TCArgumentTree> args,
                                                FunctionGenerator gen) {
        boolean isMapped = isMappedCall(args);
        List<Instruction> instructions = List.of();

        if (isMapped) {
            instructions = genMappedArgs(context, args, gen);
        }
        else {
            genInplaceArgs(args, gen);
        }

        return instructions;
    }

    public static List<Instruction> genTypeLoading(Context context, TCTree.TCClassTree node, int index) {
        if (node.superName == null
                || node.superName.isEmpty()
                || node.sym.superClass == null
                || node.sym.superClass.kind == Symbol.Kind.CLASS){
            return List.of(new LoadType(index));
        }

        if (node.sym.superClass.kind != Symbol.Kind.IMPORTED)
            throw new AssertionError(
                    "super type must be either a known class or an import");

        List<Instruction> instructions = new ArrayList<>();
        Iterator<String> itr = node.superName.iterator();
        itr.next();

        instructions.add(new LoadGlobal(node.sym.superClass.address));
        while (itr.hasNext()){
            String superName = itr.next();
            instructions.add(new LoadExternal(PoolPutter.putUtf8(context, superName)));
        }

        instructions.add(new BuildType(index));
        return instructions;
    }

    private static List<Instruction> genMappedArgs(Context context,
                                List<? extends TCTree.TCArgumentTree> args,
                                FunctionGenerator gen) {

        List<Instruction> instructions = new ArrayList<>();

        for (int i = args.size() - 1; i >= 0; i--) {
            TCTree.TCArgumentTree arg = args.get(i);
            gen.scan(arg.expression, null);
            if (arg.getName() != null) {
                instructions.add(new ToMapArg(PoolPutter.putUtf8(context, arg.getName())));
            }
            else {
                instructions.add(new ToInplaceArg());
            }
        }

        return instructions;
    }

    private static void genInplaceArgs(List<? extends TCTree.TCArgumentTree> args,
                                       FunctionGenerator gen){
        for (int i = args.size() - 1; i >= 0; i--) {
            gen.scan(args.get(i).expression, null);
        }
    }

    private static boolean isMappedCall(List<? extends TCTree.TCArgumentTree> args){
        for (ArgumentTree arg : args) {
            if (arg.getName() != null) {
                return true;
            }
        }
        return false;
    }

}
