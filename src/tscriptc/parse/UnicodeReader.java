package tscriptc.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class UnicodeReader {

    private final PushbackInputStream in;

    public UnicodeReader(InputStream in) {
        this.in = new PushbackInputStream(in);
    }

    public boolean hasNext(){
        byte i = (byte) read();
        unread(i);
        return i != -1;
    }

    public char peek(){
        int i = read();
        unread(i);
        return (char) i;
    }

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

    public void unread(int i){
        try {
            in.unread(i);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
