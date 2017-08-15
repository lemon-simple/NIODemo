package com.zs.nio.netty.bytebuf;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

/**
 * @author zhangsh
 * 
 */
public class NIOTest {

    public static void main(String[] args) throws IOException {

        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi! hell netty.. \r\n", utf8));

        System.out.println(buf.isDirect());

        System.out.println(buf.release(buf.refCnt()));
        ByteBuf buf2 = buf.slice(0, 2);

        System.out.println(buf2.toString(utf8));

        buf.setByte(0, (byte) 'J');

        System.out.println(buf2.toString(utf8));
        System.out.println((char) buf2.getByte(0));
        System.out.println("\r\n");

        System.out.println("readerIndex " + buf.readerIndex());
        System.out.println("writerIndex " + buf.writerIndex());
        System.out.println("capacity " + buf.capacity());
        System.out.println("maxCapacity " + buf.maxCapacity());

        while (buf.isReadable()) {
            System.out.print((char) buf.readByte());
        }

        System.out.println("readerIndex " + buf.readerIndex());
        System.out.println("writerIndex " + buf.writerIndex());
        System.out.println("capacity " + buf.capacity());
        System.out.println("maxCapacity " + buf.maxCapacity());

        while (buf.isWritable()) {
            buf.writeByte((char) 'A');
        }
        System.out.print(new String(buf.array()));

        System.out.println("readerIndex " + buf.readerIndex());
        System.out.println("writerIndex " + buf.writerIndex());
        System.out.println("capacity " + buf.capacity());
        System.out.println("maxCapacity " + buf.maxCapacity());

    }
}
