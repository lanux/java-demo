# Netty

Reactor 的线程模型有三种:

- 单线程模型
- 多线程模型
- 主从多线程模型

Reactor 多线程模型特点:
> 1. 有专门一个线程, 即 Acceptor 线程用于监听客户端的TCP连接请求.
> 1. IO线程（不要阻塞）：客户端连接的 IO 操作都是由一个特定的 NIO 线程池负责. 每个客户端连接都与一个特定的 NIO 线程绑定, 因此在这个客户端连接中的所有IO操作都是在同一个线程中完成的.
> 1. 客户端连接有很多, 但是 NIO 线程数是比较少的, 因此一个 NIO 线程可以同时绑定到多个客户端连接中.

Reactor 主从多线程模型特点: 
> 1. 服务器端接收客户端的连接请求不再是一个线程, 而是由一个独立的线程池组成

## Server端
启动代码示例
```java
EventLoopGroup bossGroup = new NioEventLoopGroup(1);// 指定 Acceptor 线程池大小
EventLoopGroup workerGroup = new NioEventLoopGroup();
try {
    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 1024)// BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
            .childOption(ChannelOption.SO_KEEPALIVE, true)// 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
            //这里的childHandler是服务于workerGroup的,如果直接使用
            //handler方法添加处理器,则是服务于bossGroup的
            .childHandler(new ChildChannelHandler());
    ChannelFuture f = b.bind(inetHost, inetPort).sync();
    System.out.println("Netty time Server started at port " + inetPort);
    f.channel().closeFuture().sync();
} finally {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
}
```

### EventLoopGroup
[参考](https://segmentfault.com/a/1190000007403873)
EventLoopGroup管理的线程数可以通过构造函数设置，
如果没有设置，默认取java系统变量`-Dio.netty.eventLoopThreads`，
如果该系统参数也没有指定，则为可用的CPU内核数 × 2。
channel和EventLoop是绑定的，即channel从EventLoopGroup获得一个EventLoop，并注册到该EventLoop，channel生命周期内都和该EventLoop在一起，其相关的I/O、编解码、超时处理都在同一个EventLoop中，这样可以确保这些操作都是线程安全的。

**bossGroup** 实际就是Acceptor线程池，负责处理客户端的请求接入（握手/认证），如果系统只有一个服务端端口需要监听，则建议bossGroup线程组线程数设置为1。

**workerGroup** 是真正负责I/O读写操作的线程组，通过ServerBootstrap的group方法进行设置，用于后续的Channel绑定。

Netty服务器端的 ServerSocketChannel 只绑定到了 bossGroup 中的一个线程, 因此在调用 Java NIO 的 Selector.select 处理客户端的连接请求时, 实际上是在一个线程中的, 所以对只有一个服务的应用来说, bossGroup 设置多个线程是没有什么作用的, 反而还会造成资源浪费.

经 Google, Netty 中的 bossGroup 为什么使用线程池的原因大家众所纷纭, 不过我在 stackoverflow 上找到一个比较靠谱的答案:

> the creator of Netty says multiple boss threads are useful if we share NioEventLoopGroup between different server bootstraps, but I don't see the reason for it.

因此上面的 主从多线程模型 分析是有问题, 抱歉.

> Yeah the boss group is only used for accept etc and for each ServerChannel
  only one Thread is used. So if you only call bind(...) one time only one of
  the Threads out of it will be used. [see](http://netty.narkive.com/ZOGrVgar/about-netty-4-boss-thread-do)

### ChannelPipeline
一个 Channel 包含了一个 ChannelPipeline, 而 ChannelPipeline 中又维护了一个由 ChannelHandlerContext 组成的双向链表.
这个链表的头是 HeadContext, 链表的尾是 TailContext, 并且每个 ChannelHandlerContext 中又关联着一个 ChannelHandler（`head 和 tail 并没有包含 ChannelHandler·`）
[参考](https://segmentfault.com/a/1190000007308934)

## client 端
代码示例
```
EventLoopGroup workerGroup = new NioEventLoopGroup();
try {
    Bootstrap b = new Bootstrap();
    b.group(workerGroup);
    b.channel(NioSocketChannel.class);
    b.option(ChannelOption.SO_KEEPALIVE, true);
    b.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new HelloClientIntHandler());
        }
    });

    // Start the client.
    ChannelFuture f = b.connect(host, port).sync();

    // Wait until the connection is closed.
    f.channel().closeFuture().sync();
} finally {
    workerGroup.shutdownGracefully();
}

```

客户端只需要创建一个EventLoopGroup，因为它不需要独立的线程去监听客户端连接，也没必要通过一个单独的客户端线程去连接服务端。
Netty是异步事件驱动的NIO框架，它的连接和所有IO操作都是异步的，因此不需要创建单独的连接线程。


#### 参考资料

[Netty学习三：线程模型](http://www.cnblogs.com/TomSnail/p/6158249.html)
[深入浅出Netty - EventLoop, EventLoopGroup](https://caorong.github.io/2016/12/24/head-first-netty-1/)
