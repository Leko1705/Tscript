package com.tscript.tscriptc.log;

import com.tscript.tscriptc.utils.Diagnostics;

public interface Logger {

    void error(Diagnostics.Error error);

    void warning(Diagnostics.Warning warning);

}
