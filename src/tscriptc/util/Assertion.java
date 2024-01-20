package tscriptc.util;

import java.util.Objects;

public final class Assertion {

    private Assertion(){}

    public static <T> T error(){
        throw new AssertionError();
    }

    public static <T> T error(String msg){
        throw new AssertionError(msg);
    }

    public static <T> T assertNonNull(T o){
        if (o == null)
            throw new AssertionError(new NullPointerException());
        return o;
    }

}
