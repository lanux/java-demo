package com.lanux.io.nio;

import com.lanux.io.NetConfig;
import com.lanux.io.bio.IoStream;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Created by lanux on 2017/8/6.
 */
public class NioClient extends IoStream implements Closeable {
    SocketChannel socketChannel = null;

    public NioClient() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(NetConfig.SERVER_IP, NetConfig.SERVER_PORT));
            if (socketChannel.isConnectionPending()) {
                socketChannel.finishConnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String value) throws IOException {
        byte[] bytes = value.getBytes();
        System.out.println("client write " + bytes.length + " : " + maxString(value,50));
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.clear();
        buffer.put(bytes);
        buffer.flip();
        socketChannel.write(ByteBuffer.wrap(intToByteArray(bytes.length)));
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }

    }

    @Override
    public void close() throws IOException {
        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
