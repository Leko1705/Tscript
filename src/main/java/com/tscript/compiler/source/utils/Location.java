package com.tscript.compiler.source.utils;

public record Location(int startPos, int endPos, int line) {

    public static Location emptyLocation(){
        return new Location(0, 0, 0);
    }

    private static int checkNonNegative(String spec, int i) {
        if (i < 0)
            throw new IllegalArgumentException(spec + " is < 0");
        return i;
    }

    public Location(int startPos, int endPos, int line) {
        this.startPos = checkNonNegative("startPos", startPos);
        this.endPos = checkNonNegative("endPos", endPos);
        this.line = checkNonNegative("line", line);
    }

    public static Location combine(Location l1, Location l2){
        int s = Math.min(l1.startPos, l2.startPos);
        int e = Math.max(l1.endPos, l2.endPos);
        int l = Math.min(l1.line, l2.line);
        return new Location(s, e, l);
    }

}
