package com.tscript.runtime.jit.lower.jbc;

public class JBCGenClassLoader extends ClassLoader {

    private static JBCGenClassLoader instance;

    public static JBCGenClassLoader getInstance() {
        if (instance == null) {
            instance = new JBCGenClassLoader();
        }
        return instance;
    }

    private JBCGenClassLoader() {}

    public Class<?> defineClass(byte[] b) {
        return defineClass(null, b, 0, b.length);
    }
}