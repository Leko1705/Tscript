package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.util.Location;

public record BasicToken(Location location, Object tag, String lexem) implements Token {

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getLexem() {
        return lexem;
    }

    @Override
    public Object getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "[" + tag + ", '" + lexem + "', " + location + "]";
    }
}
