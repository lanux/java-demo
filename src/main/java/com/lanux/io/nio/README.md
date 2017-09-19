# java NIO 核心概念

- [Selector](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#selector选择器)
- [Channel]()
- [Buffer]()

## Selector(选择器)
- [获取就绪事件](https://github.com/lanux/java-demo/tree/master/src/main/java/com/lanux/io/nio#一获取就绪事件)
- [SelectionKey]()
- [Selector 的基本使用流程]()
- [close and wakeup]()
- [demo]()

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