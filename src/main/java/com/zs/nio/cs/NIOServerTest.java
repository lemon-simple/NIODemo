package com.zs.nio.cs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class NIOServerTest {

    private static ByteBuffer readFromChannel = ByteBuffer.allocate(11);

    public static void main(String[] args) throws InterruptedException {

        try {
            // 1.开启serverSocket channe
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("localhost", 9091));
            // 2.serverSocket channel 阻塞模式运行
            serverSocketChannel.configureBlocking(false);
            // 3.构造selector
            Selector selector = Selector.open();
            // 4.注册ServerChannel到selector中,并注册一个OP_ACCEPT事件,等待客户端接入
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // 5.阻塞式等待事件驱动
            SocketChannel clientSocketChannel = null;
            ServerSocketChannel serverChannel = null;
            while (true) {
                TimeUnit.SECONDS.sleep(5);

                System.out.println(selector.select());

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    if (selectionKey.isAcceptable()) {// 1. 服务端已经准备好客户端接入

                        System.out.println(selectionKey.channel() == serverSocketChannel);
                        System.out.println(selectionKey.channel() + "isAcceptable");

                        serverChannel = (ServerSocketChannel) selectionKey.channel();// 1.获取事件对应的channel：ServerSocketChannel专门用来接收连接
                        clientSocketChannel = serverChannel.accept();// 2.非阻塞。获取客户端的链接

                        System.out.println("accept" + clientSocketChannel);

                        clientSocketChannel.configureBlocking(false);
                        clientSocketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        readFromChannel.clear();

                        System.out.println(selectionKey.channel() == clientSocketChannel);
                        System.out.println(selectionKey.channel() + "isReadable");

                        clientSocketChannel = (SocketChannel) selectionKey.channel();
                        clientSocketChannel.read(readFromChannel);

                        readFromChannel.clear();

                        StringBuffer bytes = new StringBuffer();
                        while (readFromChannel.hasRemaining()) {
                            bytes.append((char) readFromChannel.get());
                        }
                        System.out.println(
                                "read Form a readable channel [" + clientSocketChannel + "]" + "msg:[" + bytes + "]");

                        clientSocketChannel.register(selector, SelectionKey.OP_WRITE);

                    } else if (selectionKey.isWritable()) {

                        System.out.println(selectionKey.channel() == clientSocketChannel);
                        System.out.println(selectionKey.channel() + "isWritable");

                        clientSocketChannel = (SocketChannel) selectionKey.channel();
                        System.out.println("writable channel" + clientSocketChannel);

                        clientSocketChannel.write(ByteBuffer.wrap("hi,clinet".getBytes()));
                        System.out
                                .println("write to a writable channel [" + clientSocketChannel + "] msg:[hi, client ]");

                        clientSocketChannel.register(selector, SelectionKey.OP_READ);

                    } else {

                        System.out.println(selectionKey.channel() == clientSocketChannel);
                        System.out.println(selectionKey.channel() + "others selectionKey");
                        System.out.println("others selectionKey:" + selectionKey);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] transferToArray(Byte[] bytes) {

        byte[] bytesArray = new byte[bytes.length];
        for (int i = 0; i < bytesArray.length; i++) {
            bytesArray[i] = bytes[i];
        }
        return bytesArray;
    }
}