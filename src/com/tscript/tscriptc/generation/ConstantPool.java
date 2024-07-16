package com.tscript.tscriptc.generation;

import com.tscript.tscriptc.util.Conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public int putBool(boolean b){
        return addIfAbsent(new Bool(b));
    }

    public int putNull(){
        return addIfAbsent(new Null());
    }

    public int putArray(List<Integer> references) {
        return addIfAbsent(new Array(references));
    }

    public int putDict(List<Integer> references) {
        return addIfAbsent(new Dict(references));
    }

    public int putRange(int from, int to) {
        return addIfAbsent(new Range(putInt(from), putInt(to)));
    }

    public int putImport(String importPath) {
        return addIfAbsent(new Import(importPath));
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
            this.value = value;
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


    private static class Bool extends Entry<Boolean> {

        private Bool(boolean value) {
            super(value);
        }

        @Override
        int getPoolKind() {
            return 7;
        }
        @Override
        byte[] inBytes() {
            return new byte[]{(byte)(value ? 1 : 0)};
        }

        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("BOOLEAN " + value).getBytes());
        }
    }

    private static class Null extends Entry<Void> {

        private Null() {
            super(null);
        }

        @Override
        int getPoolKind() {
            return 8;
        }

        @Override
        byte[] inBytes() {
            return new byte[0];
        }

        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("NULL").getBytes());
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Null;
        }
    }

    private static class Array extends Entry<List<Integer>> {
        private Array(List<Integer> references) {
            super(references);
        }

        @Override
        int getPoolKind() {
            return 9;
        }

        @Override
        byte[] inBytes() {
            byte[] bytes = new byte[value.size()+1];
            bytes[0] = (byte) value.size();
            int i = 1;
            for (int ref : value)
                bytes[i++] = (byte) ref;
            return bytes;
        }

        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("ARRAY " + value).getBytes());
        }
    }

    public static class Dict extends Entry<List<Integer>> {

        private Dict(List<Integer> references) {
            super(references);
        }

        @Override
        int getPoolKind() {
            return 10;
        }

        @Override
        byte[] inBytes() {
            byte[] bytes = new byte[value.size()+1];
            bytes[0] = (byte) (value.size() / 2);
            int i = 1;
            for (int ref : value)
                bytes[i++] = (byte) ref;
            return bytes;
        }

        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("DICTIONARY " + value).getBytes());
        }
    }

    private static class Range extends Entry<Void> {

        private final int fromAddress, toAddress;

        private Range(int fromAddress, int toAddress) {
            super(null);
            this.fromAddress = fromAddress;
            this.toAddress = toAddress;
        }

        @Override
        int getPoolKind() {
            return 11;
        }

        @Override
        byte[] inBytes() {
            return new byte[]{(byte) fromAddress, (byte) toAddress};
        }

        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("RANGE " + fromAddress + " " + toAddress).getBytes());
        }
    }


    private static class Import extends Str {
        private Import(String value) {
            super(value);
        }
        @Override
        int getPoolKind() {
            return 12;
        }
        @Override
        public void writeReadable(OutputStream out) throws IOException {
            out.write(("IMPORT " + value).getBytes());
        }
    }

}
