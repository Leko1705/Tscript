package tscriptc.tools;

import tscriptc.log.Logger;

import java.io.InputStream;
import java.io.OutputStream;

public interface Compiler {

    String getName();

    int run(InputStream in, OutputStream out, Logger logger, String... args);

    default int dis(InputStream in, OutputStream out, Logger logger, String... args){
        throw new UnsupportedOperationException("runDis");
    }

}
