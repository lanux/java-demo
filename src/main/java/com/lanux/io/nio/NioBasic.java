package com.lanux.io.nio;

import com.lanux.tool.ByteUtil;
import com.lanux.tool.StringTool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * Created by lanux on 2017/9/18.
 */
public class NioBasic {

    /**
     * 非阻塞模式下,read()方法在尚未读取到任何数据时可能就返回了。所以需要关注它的int返回值，它会告诉你读取了多少字节。
     *
     * @param sc
     * @throws IOException
     */
    public String handleRead(SocketChannel sc) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(4);
        while (header.hasRemaining()) {
            sc.read(header);
        }
        header.flip();//写=>读
        int length = ByteUtil.byteArrayToInt(header.array());
        header.clear();
        ByteBuffer input = ByteBuffer.allocate(length);
        while (input.hasRemaining()) {
            sc.read(input);
        }
        input.flip();//写=>读
        String value = new String(input.array());
        System.out.println(
                Thread.currentThread().getName() + " received " + input.limit() + " : " + StringTool.maxString(
                        value, 50));
        input.clear();
        return value;
    }

    /**
     * 非阻塞模式下，write()方法在尚未写出任何内容时可能就返回了。所以需要在循环中调用write()。
     *
     * @param key
     * @throws IOException
     */
    public void handleWrite(SelectionKey key) throws IOException {
        writeMsg((SocketChannel) key.channel(), new Date().toString());
    }

    public void writeMsg(SocketChannel sc, String value) throws IOException {
        byte[] bytes = value.getBytes();
        System.out.println(
                Thread.currentThread().getName() + "write " + bytes.length + " : " + StringTool.maxString(value, 50));
        ByteBuffer buf = ByteBuffer.allocate(bytes.length + 4);
        buf.put(ByteUtil.intToByteArray(bytes.length));
        buf.put(bytes);
        buf.flip();//写=>读
        while (buf.hasRemaining()) {
            sc.write(buf);
        }
        buf.clear();//清空所有
    }
}
