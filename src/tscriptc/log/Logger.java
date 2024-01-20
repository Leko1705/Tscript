package tscriptc.log;

import tscriptc.util.Diagnostics;

public interface Logger {

    void error(Diagnostics.Error error);

    void warning(Diagnostics.Warning warning);

}
