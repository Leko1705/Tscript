package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.utils.Location;

/**
 * The base token interface holding required information about a token
 * used by the compiler.
 * @since 1.0
 * @author Lennart Köhler
 * @param <T> the Tag type of this Token
 */
public interface Token<T> {

    /**
     * Returns the location at which this token has been identified.
     * @return the location
     */
    Location getLocation();

    /**
     * Returns the actual text representation of this Token as
     * it was read.
     * @return the textual representation
     */
    String getLexeme();

    /**
     * Returns an optional tag, assigned to this token. Returns null if there
     * is none.
     * @return the tag for this token
     */
    T getTag();

    /**
     * Checks if this token is tagged with at least one of the
     * given tags.
     * @param tag the at least one required checked Tag
     * @param tags other optional checked tags
     * @return true if this token is tagged with at least one of the
     * given tags, else false
     */
    @SuppressWarnings("all")
    default boolean hasTag(T tag, T... tags){
        if (tag.equals(getTag())) return true;
        for (T t : tags)
            if (t.equals(getTag()))
                return true;
        return false;
    }

}
