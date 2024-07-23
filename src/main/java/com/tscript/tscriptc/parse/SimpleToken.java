package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.utils.Location;

/**
 * A Basic Token Implementation fulfilling the basis needs.
 * @param location the required location
 * @param lexeme the textural representation of the token
 * @param tag the associated tag
 * @param <T> the tag type
 */
public record SimpleToken<T>(Location location, String lexeme, T tag)
        implements Token<T> {

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getLexeme() {
        return lexeme;
    }

    @Override
    public T getTag() {
        return tag;
    }
}
