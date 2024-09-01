package com.tscript.runtime.utils;

import java.nio.ByteBuffer;

public final class Conversion {

    private Conversion(){}

    public static byte[] to2Bytes(int addr){
        return new byte[] {
                (byte)((addr >> 8) & 0xFF),
                (byte)(addr & 0xFF)};
    }

    public static int from2Bytes(byte b1, byte b2){
        return ((b1 & 0xff) << 8) | (b2 & 0xff);
    }

    public static byte[] getBytes(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    public static byte[] getBytes(double value){
        return ByteBuffer.allocate(8).putDouble(value).array();
    }

    public static int fromBytes(byte b1, byte b2, byte b3, byte b4){
        return ((b1 & 0xFF) << 24) |
                ((b2 & 0xFF) << 16) |
                ((b3 & 0xFF) << 8 ) |
                ((b4 & 0xFF));
    }


}
