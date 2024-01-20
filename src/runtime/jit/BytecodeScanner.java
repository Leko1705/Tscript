package runtime.jit;

import runtime.core.VirtualFunction;
import tscriptc.generation.Opcode;

public class BytecodeScanner {

    private final VirtualFunction function;

    private int ip = -1;

    private byte[] instruction;

    public BytecodeScanner(VirtualFunction function) {
        this.function = function;
        consume();
    }

    public boolean hasNext(){
        return ip < function.getInstructions().length;
    }

    public void consume(){
        ip++;
        if (!hasNext())return;
        byte[][] instructions = function.getInstructions();
        instruction = instructions[ip];
    }

    public Opcode peekOpcode(){
        return Opcode.of(instruction[0]);
    }

    public byte[] getInstruction(){
        return instruction;
    }

    public byte getArg(int index){
        return instruction[index+1];
    }

    public int getIp() {
        return ip;
    }

    public void pushBack() {
        ip--;
        byte[][] instructions = function.getInstructions();
        instruction = instructions[ip];
    }

    public void reset(){
        ip = -1;
        consume();
    }
}
