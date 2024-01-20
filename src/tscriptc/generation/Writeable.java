package tscriptc.generation;

import java.io.IOException;
import java.io.OutputStream;

public interface Writeable {

    void write(OutputStream out) throws IOException;

    void writeReadable(OutputStream out) throws IOException;

}
