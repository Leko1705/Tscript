package com.tscript.tscriptc.log;

import com.tscript.tscriptc.utils.Diagnostics;

public class StdLogger implements Logger {

    private static StdLogger logger;

    public static StdLogger getLogger() {
        if (logger == null)
            logger = new StdLogger();
        return logger;
    }

    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET = "\u001B[0m";

    private StdLogger(){}

    @Override
    public void error(Diagnostics.Error error) {
        System.err.println(error);
        System.exit(-1);
    }

    @Override
    public void warning(Diagnostics.Warning warning) {
        System.out.println(ANSI_YELLOW + warning.getMessage() + ANSI_RESET);
    }

}
