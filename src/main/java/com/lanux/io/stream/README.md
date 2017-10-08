# java 输入输出流

## 概念

#### 1.存储设备
电脑上的数据有三种存储方式：

- 外存，比如电脑上的硬盘，磁盘，U盘等
- 内存，内存条
- 缓存，缓存是在CPU里面的

#### 2.输入流（Input Stream）
程序从输入流读取数据源。数据源包括外界(键盘、文件、网络…)，即是将数据源读入到程序的通信通道

#### 3.输出流（Output Stream）
程序向输出流写入数据。将程序中的数据输出到外界（显示器、打印机、文件、网络…）的通信通道。

#### 4.数据流分类
Java中的流分为两种：
> 1)  字节流：数据流中最小的数据单元是字节，主要用于读写图片、声音、视频等二进制数据。  <br/>
> 2)  字符流：数据流中最小的数据单元是字符，在处理字符流时涉及了字符编码的转换问题。Java中的字符是Unicode编码，一个字符占用两个字节，主要用于文本文件等Unicode数据。

# java.io包体系结构
  在整个Java.io包中最重要的就是5个类和一个接口。
  5个类指的是`File`、`OutputStream`、`InputStream`、`Writer`、`Reader`，
  一个接口指的是`Serializable`。

Java I/O包含三个部分：

1. **流式部分**  <br/>
IO的主体部分，以下内容主要讲这一部分；
2. **非流式部分**  <br/>
主要包含一些辅助流式部分的类，如：File类、RandomAccessFile类和FileDescriptor等类，专门用来管理磁盘文件与目录；
3. **其他类**  <br/>
文件读取部分的与安全相关的类，如：SerializablePermission类，以及与本地操作系统相关的文件系统的类，如：FileSystem类和Win32FileSystem类和WinNTFileSystem类。


# java io流式部分

在java.io包中有四个基本类：`InputStream`、`OutputStream`及`Reader`、`Writer`类，它们分别处理字节流和字符流：

输入/输出 | 字节流 | 字符流
---|---|---
输入流 | InputStream | Reader
输出流 | OutputStream | Writer


![image](https://raw.githubusercontent.com/lanux/java-demo/master/public/img/java-IO-2.png)

什么是 Java 序列化，如何实现 Java 序列化？

序列化就是一种用来处理对象流的机制，所谓对象流也就是将对象的内容进行流化。可以对流化后的对象进行读写操作，也可将流化后的对象传输于网络之间。序列化是为了解决在对对象流进行读写操作时所引发的问题。

序列化的实现：将需要被序列化的类实现 Serializable 接口，该接口没有需要实现的方法， implements Serializable 只是为了标注该对象是可被序列化的，然后使用一个输出流(如：FileOutputStream)来构造一个ObjectOutputStream(对象流)对象，接着，使用ObjectOutputStream对象的writeObject(Object obj)方法就可以将参数为obj的对象写出(即保存其状态)，要恢复的话则用输入流。