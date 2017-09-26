package com.lanux.io.bio;

import com.lanux.tool.ByteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lanux on 2017/9/18.
 */
public class BioBasic {
    private static final int LENGTH_BYTES = 4;

    public byte[] readStream(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[LENGTH_BYTES];
        int readLen;//本次读取的字节数
        int position = 0;//已经读取数据的下一个位置
        int bodyLength = -1;// 报文内容长度
        int limit = LENGTH_BYTES;
        while ((readLen = inputStream.read(bytes, position, limit)) != -1) {
            position += readLen;
            limit -= readLen;
            if (bodyLength == -1) {
                if (position < LENGTH_BYTES) {
                    continue;
                }
                bodyLength = ByteUtil.byteArrayToInt(bytes);
                bytes = new byte[bodyLength];
                limit = bodyLength;
                position = 0;
            } else {
                if (position == bodyLength) {
                    break;
                }
            }
        }
        return bytes;
    }

    public void writeStream(OutputStream ops, byte[] bytes) throws IOException {
        ops.write(ByteUtil.intToByteArray(bytes.length));
        ops.write(bytes);
//        ops.flush();
    }

}
