package com.zs.nio.cs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class NIOClientTest {
    private static ByteBuffer readFromChannel = ByteBuffer.allocate(11);

    public static void main(String[] args) throws IOException, InterruptedException {
        final Selector selector = Selector.open();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    doWork(selector);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    doWork(selector);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    doWork(selector);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * @param selector
     * @throws IOException
     * @throws ClosedChannelException
     * @throws InterruptedException
     */
    private static void doWork(Selector selector) throws IOException, ClosedChannelException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        socketChannel.connect(new InetSocketAddress("localhost", 9091));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        while (true) {
            TimeUnit.SECONDS.sleep(5);
            System.out.println(selector.select());
            Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();

            while (selectionKeys.hasNext()) {
                SelectionKey key = selectionKeys.next();
                selectionKeys.remove();
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    System.out.println(key.channel() == socketChannel);
                    System.out.println(key.channel() + "isConnected" + channel.finishConnect());

                    channel.register(selector, SelectionKey.OP_WRITE);
                } else if (key.isReadable()) {
                    readFromChannel.clear();
                    System.out.println(key.channel() == socketChannel);
                    System.out.println(key.channel() + "isReadable");

                    SocketChannel channel = (SocketChannel) key.channel();

                    channel.read(readFromChannel);

                    readFromChannel.flip();
                    StringBuffer s = new StringBuffer();
                    while (readFromChannel.hasRemaining()) {
                        s.append((char) readFromChannel.get());
                    }
                    System.out.println("read Form a readable channel [" + channel + "]" + "msg:[" + s + "]");

                    channel.register(selector, SelectionKey.OP_WRITE);

                } else if (key.isWritable()) {
                    System.out.println(key.channel() == socketChannel);
                    System.out.println(key.channel() + "isReadable");

                    SocketChannel channel = (SocketChannel) key.channel();
                    channel.write(ByteBuffer.wrap("hi,server".getBytes()));
                    System.out.println("write to a writable channel [" + channel + "] msg:[hi, server ]");
                    channel.register(selector, SelectionKey.OP_READ);

                } else {
                    System.out.println("others selectionKey:" + key);
                }
            }
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