package com.tscript.runtime.debugger;

import java.util.Objects;

public class BreakPoint {

    public static BreakPoint of(String moduleName, int lineNumber) {
        if (moduleName == null) {
            throw new NullPointerException("moduleName is null");
        }
        if (lineNumber < 0) {
            throw new IllegalArgumentException("lineNumber is negative");
        }
        return new BreakPoint(moduleName, lineNumber);
    }

    public final String  moduleName;
    public final int line;

    private BreakPoint(String moduleName, int line) {
        this.moduleName = moduleName;
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BreakPoint that = (BreakPoint) o;
        return line == that.line && Objects.equals(moduleName, that.moduleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleName, line);
    }
}
