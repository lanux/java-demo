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
    private static final int LENGTH_VALUE_BYTES = 4;// 报文内容长度值
    private static final int BYTE_BUFFER = 1024;

    public byte[] readStream2(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] bytes = new byte[LENGTH_VALUE_BYTES];
        int readLen;//本次读取的字节数
        int hadRead = 0;//已经读取数据长度
        int bodyLength = -1;
        while ((readLen = inputStream.read(bytes)) != -1) {
            hadRead += readLen;
            if (bodyLength == -1) {
                if (hadRead < LENGTH_VALUE_BYTES) continue;
                bodyLength = ByteUtil.byteArrayToInt(bytes);
                bytes = new byte[BYTE_BUFFER];// 开始读取报文内容,字节数组用长一点的
                hadRead = 0;// 开始读取报文内容，hadRead重置0
            } else {
                outSteam.write(bytes, 0, readLen);
                if (hadRead == bodyLength) break;
            }
        }
        outSteam.flush();
        return outSteam.toByteArray();
    }

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
                if (offset < LENGTH_VALUE_BYTES)continue;
                bodyLength = ByteUtil.byteArrayToInt(bytes);
                bytes = new byte[bodyLength];// 开始读取报文内容,初始化报文长度相当的字节数组来承载报文内容
                limit = bodyLength;// 初始化 报文内容剩余待读长度= bodyLength
                offset = 0;// 开始读取报文内容，offset重置0
            } else {
                if (offset == bodyLength) {
                    break;// 报文内容已经读取完毕
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
