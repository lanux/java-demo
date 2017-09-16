package com.lanux.io.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by lanux on 2017/9/16.
 */
public class NettyClient {
    public void connect(String remoteServer, int port) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChildChannelHandler());

            ChannelFuture f = b.connect(remoteServer,port).sync();
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
//            ch.pipeline().addLast(new NettyMessageDecoder(1024 * 1024, 4, 4, -8, 0))
//                    .addLast(new NettyMessageEncoder())
//                    .addLast(new LoginAuthReqHandler());
        }

    }

    public static void main(String[] args){
        try {
            new NettyClient().connect("127.0.0.1", 9080);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
