package com.tscript.compiler.impl.generation.compiled;

import com.tscript.compiler.impl.generation.compiled.instruction.Instruction;

import java.util.List;

public interface CompiledFunction extends CompiledUnit {

    int getIndex();

    String getName();

    List<Parameter> getParameters();

    int getStackSize();

    int getRegisterAmount();

    List<Instruction> getInstructions();



    class Parameter {

        public static Parameter of(String name, int defaultValueRef){
            return new Parameter(name, defaultValueRef);
        }

        public static Parameter of(String name){
            return new Parameter(name);
        }

        public final String name;
        public final int defaultValueRef;

        private Parameter(String name, int defaultValueRef) {
            this.name = name;
            this.defaultValueRef = defaultValueRef;
        }

        private Parameter(String name) {
            this(name, -1);
        }
    }

}
