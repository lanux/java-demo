package com.lanux.io.nio;

import com.lanux.io.NetConfig;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by lanux on 2017/8/6.
 */
public class NioClient extends NioBasic implements Closeable {

    private Selector selector;
    private SocketChannel channel;

    public volatile boolean connected;

    public NioClient() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(NetConfig.SERVER_IP, NetConfig.SERVER_PORT));
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        while (true) {
            try {
                if (selector.select(NetConfig.SO_TIMEOUT) == 0) {
                    continue;
                }
                Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
                while (ite.hasNext()) {
                    SelectionKey key = ite.next();
                    //删除已选的key，防止重复处理
                    ite.remove();
                    if (key.isValid() && key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        channel.configureBlocking(false)
                                .register(selector, SelectionKey.OP_READ);
                        connected = true;

                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        handleRead(channel);
                    } else if (key.isWritable()) {
                        handleWrite(key);
//                        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE); //取消注册写监听
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String value) throws IOException {
        writeMsg(channel, value);
    }

    @Override
    public void close() throws IOException {
        try {
            connected = false;
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
