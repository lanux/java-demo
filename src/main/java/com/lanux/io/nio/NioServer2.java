package com.lanux.io.nio;

import com.lanux.io.NetConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lanux on 2017/9/16.
 */
public class NioServer2 extends NioBasic implements Closeable {
    private Selector acceptSelector;
    private ServerSocketChannel ssc;
    private Selector ioSelector;
    private volatile boolean ioThreadStarted;

    private static ExecutorService executor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
                final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "nio-worker-thread-" + threadNumber.getAndIncrement());
                }
            });

    public NioServer2() {
        try {
            acceptSelector = Selector.open();
            ioSelector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(NetConfig.SERVER_IP, NetConfig.SERVER_PORT));
            ssc.configureBlocking(false);
            ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
            new Thread(() -> listen(acceptSelector), "nio-accept-thread").start();
            System.out.println("nio server started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen(Selector selector) {
        while (true) {
            try {
                // selector.select() 阻塞到至少有一个通道在你注册的事件上就绪
                // selector.select(long timeOut) 阻塞到至少有一个通道在你注册的事件上就绪或者超时timeOut
                // selector.selectNow() 立即返回。如果没有就绪的通道则返回0,与wakeup没有太大关系。
                // select方法的返回值表示就绪通道的个数。
                if (selector.select() == 0) {
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (!key.isValid()) {
                    // 选择键无效
                    continue;
                }
                // 这里不能用异步
                handleKey(key);
            }
        }
    }

    private void handleKey(SelectionKey key) {
        try {
            if (key.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                serverSocketChannel
                        .accept()
                        .configureBlocking(false)
                        .register(ioSelector, SelectionKey.OP_READ);

                // .register(selector,SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                // 一般来说，你不应该注册写事件。
                // 写操作的就绪条件为底层缓冲区有空闲空间，而写缓冲区绝大部分时间都是有空闲空间的，所以当你注册写事件后，写操作一直是就绪的，选择处理线程全占用整个CPU资源。
                // 所以，只有当你确实有数据要写时再注册写操作，并在写完以后马上取消注册。
                //  key.interestOps(SelectionKey.OP_WRITE);  //注册写监听
                //  key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE); //取消注册写监听

                if (!ioThreadStarted) {
                    new Thread(() -> listen(ioSelector), "nio-io-thread").start();
                    ioThreadStarted = true;
                }
                // 在其他线程中在那个selector上调用wakeUp方法，使阻塞在select上的线程立即返回。
                // 如果调用wakeUp时并没有select线程阻塞，则下次调用select时会立即返回。
//                ioSelector.wakeup();
            }
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                String value = handleRead(sc);
                if (StringUtils.isBlank(value)) {
                    return;
                }
                executor.submit(() -> {
                    //里面可以写一些负责的处理逻辑
                    try {
                        writeMsg(sc, value);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            if (key.isWritable()) {
                writeMsg((SocketChannel) key.channel(), new Date().toString());
                //key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);//取消注册写监听
            }
            if (key.isConnectable()) {
                System.out.println("is connect able");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (acceptSelector != null) {
                acceptSelector.close();
            }
            if (ioSelector != null) {
                ioSelector.close();
            }
            if (ssc != null) {
                ssc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
