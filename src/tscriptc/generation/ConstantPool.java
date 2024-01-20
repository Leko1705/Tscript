package tscriptc.generation;

import tscriptc.util.Conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConstantPool implements Writeable {

    private final List<Entry<?>> entries = new ArrayList<>();

    public int putInt(int i){
        return addIfAbsent(new Int(i));
    }

    public int putReal(double d){
        return addIfAbsent(new Real(d));
    }

    public int putStr(String s){
        return addIfAbsent(new Str(s));
    }

    public int putUTF8(String s){
        return addIfAbsent(new UTF8(s));
    }

    public int putFunc(String s){
        return addIfAbsent(new Function(s));
    }

    public int putNative(String s) {
        return addIfAbsent(new Native(s));
    }

    public int putType(String s){
        return addIfAbsent(new Type(s));
    }

    private int addIfAbsent(Entry<?> entry){
        int index = entries.indexOf(entry);
        if (index != -1) return index;
        index = entries.size();
        entries.add(entry);
        return index;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(Conversion.getBytes(entries.size()));
        for (Entry<?> entry : entries)
            entry.write(out);
    }

    @Override
    public void writeReadable(OutputStream out) throws IOException {
        out.write("\nconstant-pool:\n".getBytes());
        int i = 0;
        for (Entry<?> entry : entries){
            out.write(("\t" + i++ + ": ").getBytes());
            entry.writeReadable(out);
            out.write('\n');
        }
    }



    private static abstract class Entry<T> implements Writeable {
        final T value;
        private Entry(T value) {
            this.value = Objects.requireNonNull(value);
        }
        abstract int getPoolKind();
        abstract byte[] inBytes();
        public boolean equals(Object o){
            if (!(o instanceof Entry<?> e)) return false;
            if (getPoolKind() != e.getPoolKind()) return false;
            return Arrays.equals(inBytes(), e.inBytes());
        }
        @Override
        public void write(OutputStream out) throws IOException {
            out.write(getPoolKind());
            out.write(inBytes());
        }
    }


    private static class Int extends Entry<Integer> {
        private Int(Integer value) {
            super(value);
        }
        @Override
        public int getPoolKind() {
            return 0;
        }
        @Override
        public byte[] inBytes() {
            return Conversion.getBytes(value);
        }
        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("INTEGER " + value).getBytes());
        }
    }

    private static class Real extends Entry<Double> {
        private Real(Double value) {
            super(value);
        }
        @Override
        int getPoolKind() {
            return 1;
        }
        @Override
        byte[] inBytes() {
            return Conversion.getBytes(value);
        }
        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("REAL " + value).getBytes());
        }
    }

    private static class Str extends Entry<String> {

        private Str(String value) {
            super(value);
        }

        @Override
        int getPoolKind() {
            return 2;
        }
        @Override
        byte[] inBytes() {
            return (value+'\0').getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("STRING " + value).getBytes());
        }
    }

    private static class UTF8 extends Str {
        private UTF8(String value) {
            super(value);
        }
        @Override
        int getPoolKind() {
            return 3;
        }
        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("UTF8 " + value).getBytes());
        }
    }

    private static class Function extends Str {
        private Function(String value) {
            super(value);
        }
        @Override
        int getPoolKind() {
            return 4;
        }
        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("FUNCTION " + value).getBytes());
        }
    }

    private static class Native extends Str {
        private Native(String value) {
            super(value);
        }
        @Override
        int getPoolKind() {
            return 5;
        }
        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("NATIVE " + value).getBytes());
        }
    }

    private static class Type extends Str {

        private Type(String value) {
            super(value);
        }

        @Override
        int getPoolKind() {
            return 6;
        }
        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("TYPE " + value).getBytes());
        }
    }


}
