package tscriptc.generation;

import tscriptc.util.Conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Instruction implements Writeable {

    public Opcode opcode;
    public byte[] bytes;

    public Instruction(Opcode opcode, byte... bytes){
        this.opcode = opcode;
        this.bytes = bytes;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(opcode.b);
        out.write(bytes);
    }

    @Override
    public void writeReadable(OutputStream out) throws IOException {
        out.write("\t".getBytes());
        out.write(opcode.toString().getBytes());
        if (opcode == Opcode.NEW_LINE){
            int line = Conversion.fromBytes(bytes[0], bytes[1], bytes[2], bytes[3]);
            String str = " " + line + "\n";
            out.write(str.getBytes(StandardCharsets.UTF_8));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(" ").append(b);
        out.write(sb.toString().getBytes());
        out.write('\n');
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t");
        sb.append(opcode.toString());
        for (byte b : bytes)
            sb.append(" ").append(b);
        return sb.toString();
    }
}
