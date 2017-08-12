package com.zs.nio.cs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {

    /* 标识数字 */
    private static int flag = 0;
    /* 缓冲区大小 */
    private static int BLOCK = 4096;
    /* 接受数据缓冲区 */
    private static ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);// 4KB;
    /* 发送数据缓冲区 */
    private static ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);

    /* 服务器端地址 */
    private final static InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", 8989);

    public static void main(String[] args) throws IOException {
        /**
         * SocketChannel创建的两种方式：
         * 
         * 1.客户端 ： SocketChannel.open(); socketChannel.connect(SERVER_ADDRESS);
         * 
         * 
         * 2.服务端： SocketChannel client =server.accept();
         * 当有连接建立，accept方法返回SocketChannel
         */
        SocketChannel socketChannel = SocketChannel.open();
        // You can set a SocketChannel into non-blocking mode. When you do so,
        // you can call connect(), read() and write() in asynchronous mode.
        socketChannel.configureBlocking(false);
        // 打开选择器
        Selector selector = Selector.open();
        // 注册连接服务端socket动作
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        // 异步连接操作，如 connect() read() write()
        // 即使没有建立连接也会立刻返回，使用socketChannel.finishConnect()检查连接建立是否成功,未成功会抛出异常
        socketChannel.connect(SERVER_ADDRESS);
        // 分配缓冲区大小内存

        Set<SelectionKey> selectionKeys;
        Iterator<SelectionKey> iterator;
        SelectionKey selectionKey;
        SocketChannel client;
        String receiveText;
        String sendText;
        int count = 0;

        while (true) {
            // 选择一组键，其相应的通道已为 I/O 操作准备就绪。
            // 此方法执行处于阻塞模式的选择操作。

            // This method performs a blocking selection operation. It returns
            // only after at least one channel is selected, this selector's
            // wakeup method is invoked, or the current thread is interrupted,
            // whichever comes first.
            System.out.println(selector.select());
            // 返回此选择器的已选择键集。
            selectionKeys = selector.selectedKeys();
            // System.out.println(selectionKeys.size());
            iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                selectionKey = iterator.next();
                if (selectionKey.isConnectable()) {
                    System.out.println(" this is connectable !");
                    //通过selectKey可以获取到对应的channel和selector
                   //selectionKey.selector()
                    client = (SocketChannel) selectionKey.channel();
                    
                    System.err.println(client == socketChannel);
                    // 异步连接操作，如 connect() read() write()
                    // 即使没有建立连接也会立刻返回，使用socketChannel.finishConnect()检查连接建立是否成功,未成功会抛出异常
                    if (client.isConnectionPending() && client.finishConnect()) {
                        System.out.println("完成连接!");
                        sendbuffer.clear();
                        sendbuffer.put("Hello,Server".getBytes());
                        sendbuffer.flip();
                        client.write(sendbuffer);
                    }
                    client.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    client = (SocketChannel) selectionKey.channel();
                    // 将缓冲区清空以备下次读取
                    receivebuffer.clear();
                    // 异步连接操作 read(),即使socketChannel中没有可读内容，也会立刻返回
                    count = client.read(receivebuffer);
                    if (count > 0) {
                        receiveText = new String(receivebuffer.array(), 0, count);
                        System.out.println("客户端接受服务器端数据--:" + receiveText);
                        client.register(selector, SelectionKey.OP_WRITE);// 读取后接着注册一个可写事件，为了向服务端发消息
                    }

                } else if (selectionKey.isWritable()) {
                    sendbuffer.clear();
                    client = (SocketChannel) selectionKey.channel();
                    sendText = "message from client--" + (flag++);
                    sendbuffer.put(sendText.getBytes());
                    // 将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
                    sendbuffer.flip();
                    while (sendbuffer.hasRemaining()) {// 无法保证一次全部写完，所以使用循环方式
                        client.write(sendbuffer); // 异步连接操作
                                                  // write(),什么都没写入也会返回，所以循环使用
                    }
                    System.out.println("客户端向服务器端发送数据--：" + sendText);
                    client.register(selector, SelectionKey.OP_READ);// 向服务端发送消息后，注册一个可读事件，当服务端再次返回消息时，这个事件将ready
                }
            }
            selectionKeys.clear();
        }
    }
}