package tscriptc.generation;

import tscriptc.util.Assertion;
import tscriptc.util.Conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Function implements Writeable {

    private final String name;
    private final String[] params;
    private int stackSize = 0;
    private int locals = 0;

    private int currentStackSize = 0;
    private final List<Instruction> instructions = new LinkedList<>();

    private int currParam = 0;

    public Function(String name, int params) {
        this.name = name;
        this.params = new String[params];
    }

    public void addInstruction(Instruction instruction){
        instructions.add(instruction);
    }

    public void stackChanges(int delta){
        currentStackSize += delta;
        if (currentStackSize < 0)
            currentStackSize = 0;
        if (currentStackSize > stackSize)
            stackSize = currentStackSize;
    }

    public int getInstructionStreamSize(){
        return instructions.size();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(name.getBytes(StandardCharsets.UTF_8));
        out.write('\0');
        out.write(Conversion.getBytes(params.length));
        for (String param : params){
            out.write(param.getBytes(StandardCharsets.UTF_8));
            out.write('\0');
        }
        out.write(Conversion.getBytes(stackSize + params.length));
        out.write(Conversion.getBytes(locals));
        out.write(Conversion.getBytes(instructions.size()));
        for (Instruction instruction : instructions)
            instruction.write(out);
    }

    @Override
    public void writeReadable(OutputStream out) throws IOException {
        String s = name
                + ": params=" + Arrays.toString(params)
                + " stack=" + stackSize
                + " locals=" + locals + "\n";

        out.write(s.getBytes());
        for (Instruction instruction : instructions)
            instruction.writeReadable(out);
    }

    public void setLocals(int locals) {
        this.locals = locals;
    }

    public void addParam(String name){
        params[currParam++] = name;
    }
}
