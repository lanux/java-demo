package com.lanux.io.bio;

import com.lanux.tool.ByteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lanux on 2017/9/18.
 */
public class BioBasic {
    private static final int LENGTH_VALUE_BYTES = 4; // 报文内容长度值 占用字节数

    public byte[] readStream(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[LENGTH_VALUE_BYTES];
        int readLen;//本次读取的字节数
        int bodyLength = -1;// 报文内容长度
        int offset = 0;//已经读取数据的偏移量
        int limit = LENGTH_VALUE_BYTES;// input stream 读取长度逐渐减少
        while ((readLen = inputStream.read(bytes, offset, limit)) != -1) {
            offset += readLen;
            limit -= readLen;
            if (bodyLength == -1) {
                // 报文内容长度值还未读取出来
                if (offset < LENGTH_VALUE_BYTES) {
                    continue;
                }
                bodyLength = ByteUtil.byteArrayToInt(bytes);
                bytes = new byte[bodyLength];
                limit = bodyLength;
                offset = 0;
            } else {
                // 报文内容长度已经读取
                if (offset == bodyLength) {
                    // 报文内容已经读取完毕
                    break;
                }
            }
        }
        return bytes;
    }

    public void writeStream(OutputStream ops, byte[] bytes) throws IOException {
        ops.write(ByteUtil.intToByteArray(bytes.length));
        ops.write(bytes);
        ops.flush();
    }

}
