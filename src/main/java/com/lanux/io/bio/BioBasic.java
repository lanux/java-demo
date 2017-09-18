package com.lanux.io.bio;

import com.lanux.tool.ByteUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lanux on 2017/9/18.
 */
public class BioBasic {
    private static final int LENGTH_BYTES = 4;

    public byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] totalLen = new byte[LENGTH_BYTES];
        int readLen = -1;//本次读取的字节数
        int position = 0;//已经读取数据的下一个位置
        while ((readLen = inputStream.read(totalLen, 0, totalLen.length)) != -1) {
            if (position == 0) {
                position = LENGTH_BYTES;
                int i = ByteUtil.byteArrayToInt(totalLen);
                totalLen = new byte[i];
            } else {
                outSteam.write(totalLen);
                break;
            }
        }
        outSteam.flush();
        return outSteam.toByteArray();
    }

    public void writeStream(OutputStream ops, byte[] bytes) throws IOException {
        ops.write(ByteUtil.intToByteArray(bytes.length));
        ops.write(bytes);
        ops.flush();
    }

}
