package com.tscript.tscriptc.log;

import com.tscript.tscriptc.utils.Diagnostics;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamLogger extends OutputStream implements Logger {

    private final OutputStream out;

    private int offset;

    public OutputStreamLogger(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    private void writeMessage(String message){
        try {
            write(message.getBytes(), offset, message.length());
            offset += message.length();
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void error(Diagnostics.Error error) {
        writeMessage(error.getMessage());
    }

    @Override
    public void warning(Diagnostics.Warning warning) {
        writeMessage(warning.getMessage());
    }

    @Override
    public void flush() throws IOException {
        out.flush();
        offset = 0;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
