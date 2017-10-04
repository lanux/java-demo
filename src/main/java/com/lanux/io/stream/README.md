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
> 1)  字节流：数据流中最小的数据单元是字节  <br/>
> 2)  字符流：数据流中最小的数据单元是字符， Java中的字符是Unicode编码，一个字符占用两个字节。

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

![image](https://raw.githubusercontent.com/lanux/java-demo/master/public/img/java-io-stream.png)

```
graph LR
IO流-->字符流
IO流-->字节流
字符流-->Reader
字符流-->Writer
字节流-->InputStream
字节流-->OutputStream
Reader-->BufferedReader
Reader-->LineNumberReader
Reader-->CharArrayReader
Reader-->InputStreamReader
Reader-->PipedReader
Reader-->StringReader
Reader-->FilterReader
InputStreamReader-->FileReader
FilterReader-->PushbackReader

Writer-->BufferedWriter
Writer-->CharArrayWriter
Writer-->FilterWriter
Writer-->OutputStreamWriter
Writer-->PipedWriter
Writer-->PrintWriter
Writer-->StringWriter
OutputStreamWriter-->FileWriter

InputStream-->ObjectInputStream
InputStream-->ByteArrayInputStream
InputStream-->FileInputStream
InputStream-->FilterInputStream
InputStream-->PipedInputStream
InputStream-->SequenceInputStream
InputStream-->StringBufferInputStream
FilterInputStream-->DataInputStream
FilterInputStream-->BufferedInputStream
FilterInputStream-->PushbackInputStream

OutputStream-->FileOutputStream
OutputStream-->ObjectOutputStream
OutputStream-->FilterOutputStream
FilterOutputStream-->DataOutputStream
FilterOutputStream-->BufferedOutputStream
FilterOutputStream-->PrintStream
OutputStream-->PipedOutputStream
OutputStream-->ByteArrayOutputStream
```


一、按I/O类型来总体分类：

     1. Memory 1）从/向内存数组读写数据: CharArrayReader、 CharArrayWriter、ByteArrayInputStream、ByteArrayOutputStream
                   2）从/向内存字符串读写数据 StringReader、StringWriter、StringBufferInputStream
     2.Pipe管道  实现管道的输入和输出（进程间通信）: PipedReader、PipedWriter、PipedInputStream、PipedOutputStream
     3.File 文件流。对文件进行读、写操作 ：FileReader、FileWriter、FileInputStream、FileOutputStream
     4. ObjectSerialization 对象输入、输出 ：ObjectInputStream、ObjectOutputStream
     5.DataConversion数据流 按基本数据类型读、写（处理的数据是Java的基本类型（如布尔型，字节，整数和浮点数））：DataInputStream、DataOutputStream
     6.Printing 包含方便的打印方法 ：PrintWriter、PrintStream
     7.Buffering缓冲  在读入或写出时，对数据进行缓存，以减少I/O的次数：BufferedReader、BufferedWriter、BufferedInputStream、BufferedOutputStream
     8.Filtering 滤流，在数据进行读或写时进行过滤：FilterReader、FilterWriter、FilterInputStream、FilterOutputStream过
     9.Concatenation合并输入 把多个输入流连接成一个输入流 ：SequenceInputStream

    10.Counting计数  在读入数据时对行记数 ：LineNumberReader、LineNumberInputStream
    11.Peeking Ahead 通过缓存机制，进行预读 ：PushbackReader、PushbackInputStream
    12.Converting between Bytes and Characters 按照一定的编码/解码标准将字节流转换为字符流，或进行反向转换（Stream到Reader,Writer的转换类）：InputStreamReader、OutputStreamWriter


 1) FileReader :与FileInputStream对应
           主要用来读取字符文件，使用缺省的字符编码，有三种构造函数：
　　    (1）将文件名作为字符串 ：FileReader f=new FileReader(“c:/temp.txt”);
　　    (2）构造函数将File对象作为其参数。
　　            File f=new file(“c:/temp.txt”);
　　            FileReader f1=new FileReader(f);
　　   (3)  构造函数将FileDescriptor对象作为参数
　　          FileDescriptor() fd=new FileDescriptor()
　　          FileReader f2=new FileReader(fd);
               (1) 用指定字符数组作为参数：CharArrayReader(char[])
               (2) 将字符数组作为输入流:CharArrayReader(char[], int, int)
　         读取字符串，构造函数如下： public StringReader(String s);
        2) CharArrayReader：与ByteArrayInputStream对应
　　3) StringReader : 与StringBufferInputStream对应
　　4) InputStreamReader
　　      从输入流读取字节，在将它们转换成字符:Public inputstreamReader(inputstream is);
　　5) FilterReader: 允许过滤字符流
　　      protected filterReader(Reader r);
　　6) BufferReader :接受Reader对象作为参数，并对其添加字符缓冲器，使用readline()方法可以读取一行。
　　   Public BufferReader(Reader r);

## 如何选择IO流
    1）确定是数据源和数据目的（输入还是输出）
              源:输入流 InputStream Reader
              目的:输出流 OutputStream Writer
    2）明确操作的数据对象是否是纯文本
             是:字符流Reader，Writer
             否:字节流InputStream，OutputStream
    3）明确具体的设备。
             是硬盘文件：File++：
             读取：FileInputStream,, FileReader,
              写入：FileOutputStream，FileWriter
             是内存用数组
                  byte[]：ByteArrayInputStream, ByteArrayOutputStream
                  是char[]：CharArrayReader, CharArrayWriter
             是String：StringBufferInputStream(已过时，因为其只能用于String的每个字符都是8位的字符串), StringReader, StringWriter
             是网络用Socket流
             是键盘：用System.in（是一个InputStream对象）读取，用System.out（是一个OutoutStream对象）打印
    3）是否需要转换流
            是，就使用转换流，从Stream转化为Reader，Writer：InputStreamReader，OutputStreamWriter
    4）是否需要缓冲提高效率
       是就加上Buffered：BufferedInputStream, BufferedOuputStream, BuffereaReader, BufferedWriter
    5）是否需要格式化输出



什么是 Java 序列化，如何实现 Java 序列化？

序列化就是一种用来处理对象流的机制，所谓对象流也就是将对象的内容进行流化。可以对流化后的对象进行读写操作，也可将流化后的对象传输于网络之间。序列化是为了解决在对对象流进行读写操作时所引发的问题。

序列化的实现：将需要被序列化的类实现 Serializable 接口，该接口没有需要实现的方法， implements Serializable 只是为了标注该对象是可被序列化的，然后使用一个输出流(如：FileOutputStream)来构造一个ObjectOutputStream(对象流)对象，接着，使用ObjectOutputStream对象的writeObject(Object obj)方法就可以将参数为obj的对象写出(即保存其状态)，要恢复的话则用输入流。