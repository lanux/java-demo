package com.lanux.io.nio;

import com.lanux.io.NetConfig;
import com.lanux.io.bio.IoStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by lanux on 2017/9/16.
 */
public class NioServer extends IoStream{
    ByteBuffer header=ByteBuffer.allocate(4);

    public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssChannel.accept();
        sc.configureBlocking(false);
        sc.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, null);
    }

    public void handleRead(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        long bytesRead = sc.read(header);
        ByteBuffer input = null;
        if(!header.hasRemaining()){
            int length =byteArrayToInt(header.array(),0);
            header.clear();
            input = ByteBuffer.allocate(length);
        }
        bytesRead = sc.read(input);
        while (bytesRead > 0) {
            bytesRead = sc.read(input);
        }
        input.flip();
        System.out.println(Thread.currentThread().getName() + " received " + input.limit() + " response : " + maxString(new String(input.array()), 50));
        input.clear();
    }

    public static void handleWrite(SelectionKey key) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.flip();
        SocketChannel sc = (SocketChannel) key.channel();
        while (buf.hasRemaining()) {
            sc.write(buf);
        }
        buf.compact();
    }

    public NioServer() {
        Selector selector = null;
        ServerSocketChannel ssc = null;
        try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(NetConfig.SERVER_IP, NetConfig.SERVER_PORT));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                if (selector.select(NetConfig.SO_TIMEOUT) == 0) {
                    continue;
                }
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    if (key.isWritable() && key.isValid()) {
                        handleWrite(key);
                    }
                    if (key.isConnectable()) {
                        System.out.println("isConnectable = true");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (ssc != null) {
                    ssc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
