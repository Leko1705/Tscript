package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.ArgumentTree;

import java.util.ArrayList;
import java.util.List;

public class GenUtils {

    public static List<Instruction> genCall(Context context,
                                            List<? extends TCTree.TCArgumentTree> args,
                                            Runnable genCalled,
                                            FunctionGenerator gen){
        boolean isMapped = isMappedCall(args);

        List<Instruction> instructions = List.of();

        if (isMapped) {
            instructions = genMappedArgs(context, args, gen);
            genCalled.run();
            instructions.add(new CallMapped(args.size()));
            gen.stackShrinks(-args.size() + 1);
        }
        else {
            genInplaceArgs(args, gen);
            genCalled.run();
            gen.func.getInstructions().add(new CallInplace(args.size()));
            gen.stackShrinks(-args.size() + 1);
        }

        return instructions;
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
