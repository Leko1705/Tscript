package com.tscript.tscriptc.utils;

public class Version {

    private final int minor;
    private final int major;

    public Version(int minor, int major) {
        this.minor = minor;
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public int getMajor() {
        return major;
    }
}
