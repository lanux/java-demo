# java NIO 核心概念

- [Selector](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#selector选择器)
- [Channel](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#channel)
- [Buffer](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#buffer)

## Selector(选择器)
- [获取就绪事件](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#一获取就绪事件)
- [SelectionKey](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#二selectionkey)
- [Selector 的基本使用流程](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#三selector-的基本使用流程)
- [close and wakeup](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#四close-or-wakeup-selector)
- [demo](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#五完整的-selector-例子)

Selector(选择器)是Java NIO中能够检测一到多个NIO channel，并能够知晓通道是否为诸如读/写事件做好准备的组件。这样，一个单独的线程可以管理多个channel，从而管理多个网络连接。

**例1**
```
// 创建一个选择器:
Selector selector = Selector.open();

channel.configureBlocking(false);
// 将 Channel 注册到选择器中为了使用选择器管理 Channel:
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

```

`isOpen()` —— 判断Selector是否处于打开状态。Selector对象创建后就处于打开状态了<br/>
`close()` —— 当调用了Selector对象的close()方法，就进入关闭状态。
用完Selector后调用其close()方法会关闭该Selector，
且使注册到该Selector上的所有SelectionKey实例无效。**通道本身并不会关闭**


> 注意, 如果一个 Channel 要注册到 Selector 中, 那么这个 Channel 必须是非阻塞的, 即`channel.configureBlocking(false)`;<br/>
> 因为 Channel 必须要是非阻塞的, 因此 FileChannel 是不能够使用选择器的, 因为 FileChannel 都是阻塞的.

`channel.register()`方法的第二个参数，这是一个"interest set"，意思是在通过Selector监听Channel时对什么事件感兴趣。channel触发了一个事件则说明该事件已经就绪。

- **Connect**, 即连接事件(TCP 连接), 对应于`SelectionKey.OP_CONNECT`<br/>
- **Accept**, 即确认事件, 对应于`SelectionKey.OP_ACCEPT`<br/>
- **Read**, 即读事件, 对应于`SelectionKey.OP_READ`, 表示 buffer 可读.<br/>
- **Write**, 即写事件, 对应于`SelectionKey.OP_WRITE`, 表示 buffer 可写.<br/>

> 一般来说，你不应该注册写事件。写操作的就绪条件为底层缓冲区有空闲空间，而写缓冲区绝大部分时间都是有空闲空间的，所以当你注册写事件后，写操作一直是就绪的，选择处理线程全占用整个CPU资源。
所以，只有当你确实有数据要写时再注册写操作，并在写完以后马上取消注册。

我们可以使用或运算 ` | ` 来组合多个事件, 例如:
```
int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
```
注意, 一个 Channel 仅仅可以被注册到一个 Selector 一次, 如果将 Channel 注册到 Selector 多次, 那么其实就是相当于更新 SelectionKey 的 interest set. 例如:
```
channel.register(selector, SelectionKey.OP_READ);
channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
```

注意, 我们可以动态更改 SelectedKey 中的 interest set. 例如在 OP_ACCEPT 中, 我们可以将 interest set 更新为 OP_READ, 这样 Selector 就会将这个 Channel 的 读 IO 就绪事件包含进来了.
```
key.interestOps(SelectionKey.OP_WRITE);  //注册写监听
key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE); //取消注册写监听
```

#### 一、获取就绪事件
channel注册到selector后，我们可以通过`Selector.select()`方法阻塞查询有多少个事件（注册过的事件）准备就绪.
如果`select()`方法返回值大于1, 那么我们可以通过`selector.selectedKeys()`读取就绪事件集合，然后进行处理:

**例2**
```
while(true){
    if (selector.select(TIMEOUT) == 0) {
        System.out.print(".");
        continue;
    }
    Set<SelectionKey> selectedKeys = selector.selectedKeys();

    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

    while(keyIterator.hasNext()) {

        SelectionKey key = keyIterator.next();

        if(key.isAcceptable()) {
            // a connection was accepted by a ServerSocketChannel.

        } else if (key.isConnectable()) {
            // a connection was established with a remote server.

        } else if (key.isReadable()) {
            // a channel is ready for reading

        } else if (key.isWritable()) {
            // a channel is ready for writing
        }

        keyIterator.remove();
    }
}
```

> ***注意***, 在每次迭代时, 我们都调用 "keyIterator.remove()" 将这个 key 从迭代器中删除, 因为 select() 方法仅仅是简单地将就绪的 IO 操作放到 selectedKeys 集合中, 因此如果我们从 selectedKeys 获取到一个 key, 但是没有将它删除, 那么下一次 select 时, 这个 key 所对应的 IO 事件还在 selectedKeys 中.

一个Selector对象会包含3种类型的SelectionKey集合：<br/>
**all-keys集合** —— 当前所有向Selector注册的SelectionKey的集合，Selector的keys()方法返回该集合<br/>
**\* selected-keys集合(就绪事件集合)** —— 相关事件已经被Selector捕获的SelectionKey的集合，Selector的selectedKeys()方法返回该集合<br/>
**cancelled-keys集合** —— 已经被取消的SelectionKey的集合，Selector**没有**提供访问这种集合的方法<br/>

当register()方法执行时，新建一个SelectioKey，并把它加入Selector的all-keys集合中。<br/>
如果关闭了与SelectionKey对象关联的Channel对象，或者调用了SelectionKey对象的cancel方法，这个SelectionKey对象就会被加入到cancelled-keys集合中，表示这个SelectionKey对象已经被取消。<br/>
在执行Selector的select()方法时，如果与SelectionKey相关的事件发生了，这个SelectionKey就被加入到selected-keys集合中，程序直接调用selected-keys集合的remove()方法，或者调用它的iterator的remove()方法，都可以从selected-keys集合中删除一个SelectionKey对象。

#### 二、SelectionKey

如**例1**所示, 当我们使用 register 注册一个 Channel 时, 会返回一个 SelectionKey 对象,；
如**例2**所示`selector.selectedKeys()`返回SelectionKey集合，这个SelectionKey对象包含了如下内容:

- **interest set**, 即我们感兴趣的事件集, 即在调用 register 注册 channel 时所设置的 interest set.
- **ready set**
- **channel**
- **selector**
- **attached object**, 可选的附加对象

##### interest set
```
int interestSet = selectionKey.interestOps();
boolean isInterestedInAccept  = interestSet & SelectionKey.OP_ACCEPT;
boolean isInterestedInConnect = interestSet & SelectionKey.OP_CONNECT;
boolean isInterestedInRead    = interestSet & SelectionKey.OP_READ;
boolean isInterestedInWrite   = interestSet & SelectionKey.OP_WRITE;
```
##### ready set

代表了 Channel 所准备好了的操作.
我们可以像判断 interest set 一样操作 Ready set, 但是我们还可以使用如下方法进行判断:
```
int readySet = selectionKey.readyOps();
selectionKey.isAcceptable();
selectionKey.isConnectable();
selectionKey.isReadable();
selectionKey.isWritable();
```

##### Channel 和 Selector
我们可以通过 SelectionKey 获取相对应的 Channel 和 Selector:

Channel  channel  = selectionKey.channel();
Selector selector = selectionKey.selector();

##### Attaching Object
我们可以在selectionKey中附加一个对象:
```
selectionKey.attach(theObject);
Object attachedObj = selectionKey.attachment();
```
或者在注册时直接附加:
```
SelectionKey key = channel.register(selector, SelectionKey.OP_READ, theObject);
```


#### 三、Selector 的基本使用流程
    1. 通过 Selector.open() 打开一个 Selector.</br/>
    2. 将 Channel 注册到 Selector 中, 并设置需要监听的事件(interest set)</br/>
    3. 不断重复:</br/>
        - 调用 select() 方法</br/>
        - 调用 selector.selectedKeys() 获取 selected keys</br/>
        - 迭代每个 selected key:</br/>
            - *从 selected key 中获取 对应的 Channel 和附加信息(如果有的话)</br/>
            - *判断是哪些 IO 事件已经就绪了, 然后处理它们. 如果是 OP_ACCEPT 事件, 则调用 "SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept()" 获取 SocketChannel, 并将它设置为 非阻塞的, 然后将这个 Channel 注册到 Selector 中.</br/>
            - *根据需要更改 selected key 的监听事件.</br/>
            - *将已经处理过的 key 从 selected keys 集合中删除.</br/>

#### 四、close or wakeUp Selector
当调用了 `Selector.close()` 方法时, 我们其实是关闭了 Selector 本身并且将所有的 SelectionKey 失效, 但是并不会关闭 Channel.

某个线程调用select()方法后阻塞了，即使没有通道已经就绪，也有办法让其从select()方法返回。只要让其他线程在第一个线程调用select()方法的那个对象上调用`Selector.wakeup()`方法即可。阻塞在select()方法上的线程会立马返回。
#### 五、完整的 Selector 例子
```java
public class NioEchoServer {
    private static final int BUF_SIZE = 256;
    private static final int TIMEOUT = 3000;

    public static void main(String args[]) throws Exception {
        // 打开服务端 Socket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 打开 Selector
        Selector selector = Selector.open();

        // 服务端 Socket 监听8080端口, 并配置为非阻塞模式
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);

        // 将 channel 注册到 selector 中.
        // 通常我们都是先注册一个 OP_ACCEPT 事件, 然后在 OP_ACCEPT 到来时, 再将这个 Channel 的 OP_READ
        // 注册到 Selector 中.
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 通过调用 select 方法, 阻塞地等待 channel I/O 可操作
            if (selector.select(TIMEOUT) == 0) {
                System.out.print(".");
                continue;
            }

            // 获取 I/O 操作就绪的 SelectionKey, 通过 SelectionKey 可以知道哪些 Channel 的哪类 I/O 操作已经就绪.
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {

                SelectionKey key = keyIterator.next();

                // 当获取一个 SelectionKey 后, 就要将它删除, 表示我们已经对这个 IO 事件进行了处理.
                keyIterator.remove();

                if (key.isAcceptable()) {
                    // 当 OP_ACCEPT 事件到来时, 我们就有从 ServerSocketChannel 中获取一个 SocketChannel,
                    // 代表客户端的连接
                    // 注意, 在 OP_ACCEPT 事件中, 从 key.channel() 返回的 Channel 是 ServerSocketChannel.
                    // 而在 OP_WRITE 和 OP_READ 中, 从 key.channel() 返回的是 SocketChannel.
                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    //在 OP_ACCEPT 到来时, 再将这个 Channel 的 OP_READ 注册到 Selector 中.
                    // 注意, 这里我们如果没有设置 OP_READ 的话, 即 interest set 仍然是 OP_CONNECT 的话, 那么 select 方法会一直直接返回.
                    clientChannel.register(key.selector(), OP_READ, ByteBuffer.allocate(BUF_SIZE));
                }

                if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    long bytesRead = clientChannel.read(buf);
                    if (bytesRead == -1) {
                        clientChannel.close();
                    } else if (bytesRead > 0) {
                        key.interestOps(OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("Get data length: " + bytesRead);
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    buf.flip();
                    SocketChannel clientChannel = (SocketChannel) key.channel();

                    clientChannel.write(buf);

                    if (!buf.hasRemaining()) {
                        key.interestOps(OP_READ);//动态注册读事件
                    }
                    buf.compact();
                }
            }
        }
    }
}
```

## Channel
通常来说, 所有的 NIO 的 I/O 操作都是从 Channel 开始的. 一个 channel 类似于一个 stream.
java Stream 和 NIO Channel 对比

1. channel 既可以从通道中读取数据，又可以写数据到通道。但Stream的读写通常是单向的。
1. Channel 可以非阻塞读写, 而 Stream 是阻塞的同步读写.
1. Channel 总是从 Buffer 中读取数据, 或将数据写入到 Buffer 中.

Channel 类型有:
- FileChannel, 文件操作
- [SocketChannel](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#socketchannel), TCP 操作
- [ServerSocketChannel](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#serversocketchannel), TCP 操作, 使用在服务器端.
- [DatagramChannel](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#datagramchannel), UDP 操作

```
public static void main( String[] args ) throws Exception
{
    RandomAccessFile aFile = new RandomAccessFile("/Users/xiongyongshun/settings.xml", "rw");
    FileChannel inChannel = aFile.getChannel();

    ByteBuffer buf = ByteBuffer.allocate(48);

    int bytesRead = inChannel.read(buf);
    while (bytesRead != -1) {
        buf.flip();

        while(buf.hasRemaining()){
            System.out.print((char) buf.get());
        }

        buf.clear();
        bytesRead = inChannel.read(buf);
    }
    aFile.close();
}
```

> 注意, FileChannel 不能设置为非阻塞模式.


#### SocketChannel

SocketChannel 是一个客户端用来进行 TCP 连接的 Channel.
创建一个 SocketChannel 的方法有两种:
- 打开一个 SocketChannel, 然后将其连接到某个服务器中
- 当一个 ServerSocketChannel 接受到连接请求时, 会返回一个 SocketChannel 对象.
##### 打开 SocketChannel
```
SocketChannel socketChannel = SocketChannel.open();
socketChannel.connect(new InetSocketAddress("http://example.com", 80));
```
##### 关闭
```
socketChannel.close();
```
##### 读取数据
```
ByteBuffer buf = ByteBuffer.allocate(48);
int bytesRead = socketChannel.read(buf);
```

> read()返回 -1, 那么表示连接中断了.

##### 写入数据
```
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());

buf.flip();

while(buf.hasRemaining()) {
    channel.write(buf);
}
```

##### 非阻塞模式

我们可以设置 SocketChannel 为异步模式, 这样我们的 connect, read, write 都是异步的了.

```
socketChannel.configureBlocking(false);
socketChannel.connect(new InetSocketAddress("http://example.com", 80));

while(! socketChannel.finishConnect() ){
    //wait, or do something else...
}
```
在非阻塞模式中, 或许连接还没有建立, connect 方法就返回了, 因此我们需要检查当前是否是连接到了主机, 因此通过一个 while 循环来判断.


#### ServerSocketChannel

ServerSocketChannel 顾名思义, 是用在服务器为端的, 可以监听客户端的 TCP 连接, 例如:
```
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));
while(true){
    SocketChannel socketChannel =
            serverSocketChannel.accept();

    //do something with socketChannel...
}
```

##### 打开 关闭
```
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.close();
```
##### 监听连接

我们可以使用ServerSocketChannel.accept()方法来监听客户端的 TCP 连接请求, accept()方法会阻塞, 直到有连接到来, 当有连接时, 这个方法会返回一个 SocketChannel 对象:
```
while(true){
    SocketChannel socketChannel =
            serverSocketChannel.accept();

    //do something with socketChannel...
}
```
##### 非阻塞模式

在非阻塞模式下, accept()是非阻塞的, 因此如果此时没有连接到来, 那么 accept()方法会返回null:
```
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

serverSocketChannel.socket().bind(new InetSocketAddress(9999));
serverSocketChannel.configureBlocking(false);

while(true){
    SocketChannel socketChannel =
            serverSocketChannel.accept();

    if(socketChannel != null){
        //do something with socketChannel...
        }
}
```

#### DatagramChannel
DatagramChannel 是用来处理 UDP 连接的.

##### 打开
```
DatagramChannel channel = DatagramChannel.open();
channel.socket().bind(new InetSocketAddress(9999));
```
##### 读取数据
```
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();

channel.receive(buf);
```
##### 发送数据
```
String newData = "New String to write to file..."
                    + System.currentTimeMillis();

ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();

int bytesSent = channel.send(buf, new InetSocketAddress("example.com", 80));
```
##### 连接到指定地址

因为 UDP 是非连接的, 因此这个的 connect 并不是像 TCP 一样真正意义上的连接, 而是它会将 DatagramChannel 锁住, 因此我们仅仅可以从指定的地址中读取或写入数据.
```
channel.connect(new InetSocketAddress("example.com", 80));
```

## Buffer
当我们需要与 NIO Channel 进行交互时, 我们就需要使用到 NIO Buffer, 即数据从 Buffer读取到 Channel 中, 并且从 Channel 中写入到 Buffer 中.
实际上, 一个 Buffer 其实就是一块内存区域, 我们可以在这个内存区域中进行数据的读写. NIO Buffer 其实是这样的内存块的一个封装, 并提供了一些操作方法让我们能够方便地进行数据的读写.
Buffer 类型有:
- ByteBuffer
- CharBuffer
- DoubleBuffer
- FloatBuffer
- IntBuffer
- LongBuffer
- ShortBuffer
这些 Buffer 覆盖了能从 IO 中传输的所有的 Java 基本数据类型.

为了理解Buffer的工作原理，需要熟悉它的三个属性：

- capacity代表这块Buffer的容量(DoubleBuffer, 其Capacity是100, 那么我们最多可以写入100个double值)
- position代表当前可读（或写）的位置，初始的position值为0，每读/写一单位数据position的值递增1，直到limit结束，最大可为capacity – 1。
- limit代表本次读（或写）的右边界位置，表示你最多能对Buffer读（或写）多少数据，初始limit的值等于Buffer的capacity

> 其中 position 和 limit 的含义与 Buffer 处于读模式或写模式有关, 而 capacity 的含义与 Buffer 所处的模式无关.
capacity

#### 模式切换:

1. 将 NIO Buffer 转换为读模式
  - 调用 Buffer.flip()方法
2. 将 Buffer 转换为写模式
  - Buffer.clear() 清空整个buffer
  - Buffer.compact() 清空已读部分

#### 关于 Direct Buffer 和 Non-Direct Buffer 的区别
##### Direct Buffer:
所分配的内存不在 JVM 堆上, 不受 GC 的管理.(但是 Direct Buffer 的 Java 对象是由 GC 管理的, 因此当发生 GC, 对象被回收时, Direct Buffer 也会被释放)
因为 Direct Buffer 不在 JVM 堆上分配, 因此 Direct Buffer 对应用程序的内存占用的影响就不那么明显(实际上还是占用了这么多内存, 但是 JVM 不好统计到非 JVM 管理的内存.)
申请和释放 Direct Buffer 的开销比较大. 因此正确的使用 Direct Buffer 的方式是在初始化时申请一个 Buffer, 然后不断复用此 buffer, 在程序结束后才释放此 buffer.
使用 Direct Buffer 时, 当进行一些底层的系统 IO 操作时, 效率会比较高, 因为此时 JVM 不需要拷贝 buffer 中的内存到中间临时缓冲区中.
##### Non-Direct Buffer:
直接在 JVM 堆上进行内存的分配, 本质上是 byte[] 数组的封装.
因为 Non-Direct Buffer 在 JVM 堆中, 因此当进行操作系统底层 IO 操作中时, 会将此 buffer 的内存复制到中间临时缓冲区中. 因此 Non-Direct Buffer 的效率就较低.

#### 写入数据到 Buffer
```
int bytesRead = inChannel.read(buf); //read into buffer.
buf.put(127);
从 Buffer 中读取数据
//read from buffer into channel.
int bytesWritten = inChannel.write(buf);
byte aByte = buf.get();
```
#### 重置 position
Buffer.rewind()方法可以重置 position 的值为0, 因此我们可以重新读取/写入 Buffer 了.
如果是读模式, 则重置的是读模式的 position, 如果是写模式, 则重置的是写模式的 position.
例如:

> rewind() 主要针对于读模式. 在读模式时, 读取到 limit 后, 可以调用 rewind() 方法, 将读 position 置为0.

#### mark()和 reset()
我们可以通过调用 Buffer.mark()将当前的 position 的值保存起来, 随后可以通过调用 Buffer.reset()方法将 position 的值回复回来.

#### flip, rewind, clear, compact 的区别

flip 方法源码
```
public final Buffer flip() {
    limit = position;
    position = 0;
    mark = -1;
    return this;
}
```
Buffer 的读/写模式共用一个 position 和 limit 变量.
当从写模式变为读模式时, 原先的 写 position 就变成了读模式的 limit.

rewind 方法源码
```
public final Buffer rewind() {
    position = 0;
    mark = -1;
    return this;
}
```
rewind, 即倒带, 这个方法仅仅是将 position 置为0.

clear 方法源码:
```
public final Buffer clear() {
    position = 0;
    limit = capacity;
    mark = -1;
    return this;
}
```
根据源码我们可以知道, clear 将 positin 设置为0, 将 limit 设置为 capacity.

compact 方法源码：
```
public ByteBuffer compact() {
    System.arraycopy(hb, ix(position()), hb, ix(0), remaining());
    position(remaining());
    limit(capacity());
    discardMark();
    return this;
}
```
#### Buffer 的比较
我们可以通过 equals() 或 compareTo() 方法比较两个 Buffer, 当且仅当如下条件满足时, 两个 Buffer 是相等的:

- 两个 Buffer 是相同类型的
- 两个 Buffer 的剩余的数据个数是相同的
- 两个 Buffer 的剩余的数据都是相同的.

通过上述条件我们可以发现, 比较两个 Buffer 时, 并不是 Buffer 中的每个元素都进行比较, 而是比较 Buffer 中剩余的元素.