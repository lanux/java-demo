
##### Kafka的优点？
 - 高吞吐，低延迟
 - 持久性、可靠性
 - 容错性
 - 高并发
 - 可扩展性
 
 
![图片](./img/ar.png)
 
#### 名词解析
- **Producer** ：消息生产者，就是向 kafka broker 发消息的客户端。
- **Consumer** ：消息消费者，向 kafka broker 取消息的客户端。
- **Topic** ：可以理解为一个队列，一个 Topic 又分为一个或多个分区，
- **Consumer Group**：这是 kafka 用来实现一个 topic 消息的广播（发给所有的 consumer）和单播（发给任意一个 consumer）的手段。一个 topic 可以有多个 Consumer Group。
- **Broker** ：一台 kafka 服务器就是一个 broker。一个集群由多个 broker 组成。一个 broker 可以容纳多个 topic。
- **Partition**：为了实现扩展性，一个非常大的 topic 可以分布到多个 broker上，每个 partition 是一个有序的队列。partition 中的每条消息都会被分配一个有序的id（offset）。将消息发给 consumer，kafka 只保证按一个 partition 中的消息的顺序，不保证一个 topic 的整体（多个 partition 间）的顺序。
- **Offset**：kafka 的存储文件都是按照 offset.kafka 来命名，用 offset 做名字的好处是方便查找。例如你想找位于 2049 的位置，只要找到 2048.kafka 的文件即可。当然 the first offset 就是 00000000000.kafka。

[参考](https://www.jianshu.com/p/d0fc412bcf46)
