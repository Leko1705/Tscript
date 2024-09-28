package com.tscript.compiler.tools;

import java.io.InputStream;
import java.io.OutputStream;

public interface Tool {

    void run(InputStream in, OutputStream out, String[] args);

}
