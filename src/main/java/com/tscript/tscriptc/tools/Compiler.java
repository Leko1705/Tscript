package com.tscript.tscriptc.tools;

import com.tscript.tscriptc.log.Logger;

import java.io.InputStream;
import java.io.OutputStream;

public interface Compiler {

    int run(InputStream in, OutputStream out, Logger logger, String[] args);

}
