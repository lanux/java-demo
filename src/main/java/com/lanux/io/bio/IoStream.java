package com.lanux.io.bio;

import java.io.*;

/**
 * Created by lanux on 2017/8/6.
 */
public class IoStream {

    private static final int LENGTH_BYTES = 4;

    public byte[] intToByteArray(final int value) {
        int byteNum = (40 - Integer.numberOfLeadingZeros(value < 0 ? ~value : value)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++)
            byteArray[3 - n] = (byte) (value >>> (n * 8));

        return (byteArray);
    }

    public int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    public byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] totalLen = new byte[LENGTH_BYTES];
        int readLen = -1;//本次读取的字节数
        int position = 0;//已经读取数据的下一个位置
        while ((readLen = inputStream.read(totalLen, 0, totalLen.length)) !=-1) {
            if (position==0)
            {
                position = LENGTH_BYTES;
                int i = byteArrayToInt(totalLen, 0);
                totalLen = new byte[i];
            }else {
                outSteam.write(totalLen);
                break;
            }
        }
        outSteam.flush();
        return outSteam.toByteArray();
    }

    public void writeStream(OutputStream ops,  byte[] bytes) throws IOException {
        ops.write(intToByteArray(bytes.length));
        ops.write(bytes);
        ops.flush();
    }

    public String maxString(String value,int length) {
        if (value==null||value.length()<=length){
            return value;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(value.substring(0,length/2));
        sb.append("***");
        sb.append(value.substring(value.length()-length/2,value.length()));
        return sb.toString();
    }

}
