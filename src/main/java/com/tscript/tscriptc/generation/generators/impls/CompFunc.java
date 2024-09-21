package com.tscript.tscriptc.generation.generators.impls;

import com.tscript.tscriptc.generation.compiled.CompiledFunction;
import com.tscript.tscriptc.generation.compiled.instruction.Instruction;

import java.util.ArrayList;
import java.util.List;

public class CompFunc implements CompiledFunction {

    public final String name;
    public final List<Parameter> parameters = new ArrayList<>();
    private final List<Instruction> instructions = new ArrayList<>();

    private final int index;
    public int stackSize;
    public int locals;

    public CompFunc(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public int getStackSize() {
        return stackSize;
    }

    @Override
    public int getRegisterAmount() {
        return locals;
    }

    @Override
    public List<Instruction> getInstructions() {
        return instructions;
    }

}
