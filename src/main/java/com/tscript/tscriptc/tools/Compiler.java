package com.tscript.tscriptc.tools;

import java.io.InputStream;
import java.io.OutputStream;

public interface Compiler extends Tool {

    void run(InputStream in, OutputStream out, String[] args);

}
