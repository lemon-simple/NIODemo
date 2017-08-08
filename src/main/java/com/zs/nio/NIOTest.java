package com.zs.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhangsh
 * 
 */
public class NIOTest {

    public static void main(String[] args) throws IOException {
        // RandomAccessFile与其他IOStream最大却别别
        // 就是可以通过内部指针可以指定文件读写的具体位置
        RandomAccessFile randomAccessFile = new RandomAccessFile("A.txt", "rw");
        randomAccessFile.seek(0);// 从5byte位置开始读
        FileChannel fileChannel = randomAccessFile.getChannel();
        // 初始化1M连续内存空间
        ByteBuffer buf = ByteBuffer.allocate(1024);
        System.out.println("init buffer:    " + buf);
        // 从inChannel中读取数据，放入buf中
        int bytesRead = fileChannel.read(buf);

        System.out.println("buffer after has been  written:  " + buf);
        // flip()调用，然后开始读
        buf.flip();

        System.out.println("buffer after flip:   " + buf);
        while (buf.hasRemaining()) {
            System.out.println((char) buf.get());
            System.out.println((char) buf.get());

            System.out.println("buffer after get:    " + buf);
//            buf.compact();
            // System.out.println("buffer after compact: " + buf);
        }
        buf.clear();
        randomAccessFile.close();
    }
}
