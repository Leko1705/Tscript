package com.tscript.tscriptc.log;

import com.tscript.tscriptc.utils.Diagnostics;

public interface Logger {

    void error(Diagnostics.Error error);

    void warning(Diagnostics.Warning warning);


    static Logger getStandardLogger(){

        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_RESET = "\u001B[0m";

        return new Logger() {

            @Override
            public void error(Diagnostics.Error error) {
                System.err.println(error.toString());
                System.exit(-1);
            }

            @Override
            public void warning(Diagnostics.Warning warning) {
                System.out.println(ANSI_YELLOW + warning.getMessage() + ANSI_RESET);
            }

            @Override
            public String toString() {
                return "DefaultLogger";
            }
        };
    }

}
