package com.zs.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhangsh A.txt: 12345678900987654321
 */
public class NIOScatterAndGather {

    public static void main(String[] args) throws IOException {
        // RandomAccessFile与其他IOStream最大却别别
        // 就是可以通过内部指针可以指定文件读写的具体位置
        RandomAccessFile randomAccessFile = new RandomAccessFile("A.txt", "rw");
        randomAccessFile.seek(0);// 从5byte位置开始读
        FileChannel fileChannel = randomAccessFile.getChannel();
        // 初始化1M连续内存空间
        ByteBuffer buf1 = ByteBuffer.allocate(10);
        ByteBuffer buf2 = ByteBuffer.allocate(10);

        ByteBuffer[] bufferArray = { buf1, buf2 };
        fileChannel.read(bufferArray);// scatter:会依次写入byteBuffer中，前一个byteBuffer写满了,继续写第二个,以此类推

        // flip()调用，然后开始读
        buf1.flip();
        buf2.flip();

        if (buf1.hasRemaining()) {
            System.out.println("buf1 " + buf1);
            System.out.println(new String(buf1.array()));// 1234567890
        }
        if (buf2.hasRemaining()) {
            System.out.println("buf2 " + buf2);
            System.out.println(new String(buf2.array()));// 0987654321
        }
        ByteBuffer bufEmpty = ByteBuffer.wrap(new String("  ").getBytes());
        fileChannel.write(new ByteBuffer[] { bufEmpty, buf1, buf2 });// gather:会按照数组入参的顺序读取buffer中position到limit之间的元素
        buf1.clear();
        buf2.clear();

        randomAccessFile.close();
    }
    // console output:
    // 12345678900987654321 12345678900987654321
}
