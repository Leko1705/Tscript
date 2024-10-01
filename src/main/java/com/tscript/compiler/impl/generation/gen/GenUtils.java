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
            for (TCTree.TCArgumentTree arg : args){
                gen.scan(arg.expression, null);
            }
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
            for (TCTree.TCArgumentTree arg : args){
                gen.scan(arg.expression, null);
            }
        }

        return instructions;
    }

    private static List<Instruction> genMappedArgs(Context context,
                                List<? extends TCTree.TCArgumentTree> args,
                                FunctionGenerator gen) {

        List<Instruction> instructions = new ArrayList<>();

        for (TCTree.TCArgumentTree arg : args) {
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

    private static boolean isMappedCall(List<? extends TCTree.TCArgumentTree> args){
        for (ArgumentTree arg : args) {
            if (arg.getName() != null) {
                return true;
            }
        }
        return false;
    }

}
