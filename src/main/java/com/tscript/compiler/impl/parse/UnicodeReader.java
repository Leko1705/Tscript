package com.tscript.compiler.impl.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * A Reader for Reading char by char from an <code>InputStream</code>.
 * This Reader uses the peek and consume pattern.
 * @since 1.0
 * @author Lennart Köhler
 */
public class UnicodeReader {

    private final PushbackInputStream in;

    /**
     * Creates a new UnicodeReader from the given InputStream.
     * @param in the inputStream this UnicodeReader is based on
     */
    public UnicodeReader(InputStream in) {
        this.in = new PushbackInputStream(in);
    }

    /**
     * Returns weather this reader has a next char to read.
     * @return true if this reader has a next char, else false.
     */
    public boolean hasNext(){
        byte i = (byte) read();
        unread(i);
        return i != -1;
    }

    /**
     * Returns the current char from this reader without consuming it.
     * @return the current char
     * @see #consume()
     */
    public char peek(){
        int i = read();
        unread(i);
        return (char) i;
    }

    /**
     * Returns the current char of this reader and moves to the next one.
     * @return the current char
     */
    public char consume(){
        return (char) read();
    }

    private int read(){
        try {
            return in.read();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void unread(int i){
        try {
            in.unread(i);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
