package com.tscript.tscriptc.log;

import com.tscript.tscriptc.utils.Diagnostics;

public class VoidLogger implements Logger {

    @Override
    public void error(Diagnostics.Error error) { }

    @Override
    public void warning(Diagnostics.Warning warning) { }
}
