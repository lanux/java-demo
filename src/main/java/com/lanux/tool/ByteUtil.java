package com.lanux.tool;

/**
 * Created by lanux on 2017/9/18.
 */
public class ByteUtil {

    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    public static byte intToByte(int x) {
        return (byte) x;
    }

    public static int byteToInt(byte b) {
        return b & 0xFF;
    }

    public static int byteArrayToInt(byte[] b) {
        return byteToInt(b[3]) |
                byteToInt(b[2]) << 8 |
                byteToInt(b[1]) << 16 |
                byteToInt(b[0]) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
}
