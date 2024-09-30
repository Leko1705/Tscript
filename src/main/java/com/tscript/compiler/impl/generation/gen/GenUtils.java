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
        boolean isMapped = false;
        for (ArgumentTree arg : args) {
            if (arg.getName() != null) {
                isMapped = true;
                break;
            }
        }

        List<Instruction> instructions = new ArrayList<>();

        if (isMapped) {
            for (TCTree.TCArgumentTree arg : args) {
                gen.scan(arg.expression, null);
                if (arg.getName() != null) {
                    instructions.add(new ToMapArg(PoolPutter.putUtf8(context, arg.getName())));
                }
                else {
                    instructions.add(new ToInplaceArg());
                }
            }
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

}
