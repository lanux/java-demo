package com.lanux.io.netty;

import com.lanux.io.NetConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lanux on 2017/9/16.
 */
public class NettyClient {
    public void connect(String remoteServer, int port) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup(0,
                new ThreadFactory() {
                    AtomicInteger count = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NioEventLoop-" + count.getAndIncrement());
                    }
                });
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, NetConfig.SO_TIMEOUT)
                    .handler(new ChildChannelHandler());

            ChannelFuture f = b.connect(remoteServer, port).sync();
            System.out.println("Netty time Client connected at port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static class ChildChannelHandler extends
            ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS))
                    .addLast(new NettyMessageDecoder());
//                    .addLast(new NettyMessageEncoder())
//                    .addLast(new LoginAuthReqHandler());
        }

    }

    public static void main(String[] args) {
        try {
            new NettyClient().connect("127.0.0.1", 9080);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
