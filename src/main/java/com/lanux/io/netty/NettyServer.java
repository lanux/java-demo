package com.lanux.io.netty;

import com.lanux.io.NetConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by lanux on 2017/9/16.
 */
public class NettyServer {
    public void bind(String inetHost, int inetPort) throws Exception {

        /**
         * Netty 的服务器端的 acceptor 阶段, 没有使用到多线程,主从多线程模型 在 Netty 的服务器端是不存在的.
         * 服务器端的 ServerSocketChannel 只绑定到了 bossGroup 中的一个线程,
         * 因此在调用 Java NIO 的 Selector.select 处理客户端的连接请求时, 实际上是在一个线程中的,
         * 所以对只有一个服务的应用来说, bossGroup 设置多个线程是没有什么作用的, 反而还会造成资源浪费.
         * the creator of Netty says multiple boss threads are useful if we share NioEventLoopGroup
         * between different server bootstraps, but I don't see the reason for it.
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);// 指定 Acceptor 线程池大小
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)// BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    .childOption(ChannelOption.SO_KEEPALIVE, true)// 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                    .childHandler(new ChildChannelHandler());
            ChannelFuture f = b.bind(inetHost, inetPort).sync();
            System.out.println("Netty time Server started at port " + inetPort);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static class ChildChannelHandler extends
            ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new NettyMessageDecoder())
                    .addLast(new NettyMessageEncoder())
                    .addLast(new NettyMessageHandler());
        }

    }

    public static void main(String[] args) {
        try {
            new NettyServer().bind(NetConfig.SERVER_IP, NetConfig.SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
