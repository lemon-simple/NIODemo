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
public class NIOGetReadTest {

    public static void main(String[] args) throws IOException {
        byteBufWriteRead();
    }

    public static void byteBufWriteRead() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!是的", utf8);
        System.out.println((char) buf.readByte());
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        buf.writeByte((byte) '?');

        System.out.println((readerIndex == buf.readerIndex()) + "oldReaderIndex     " + readerIndex
                + "  ReaderIndex     " + buf.readerIndex());
        System.out.println((writerIndex == buf.writerIndex()) + "oldWriterIndex  " + writerIndex + "    WriterIndex  "
                + buf.writerIndex());
    }

    public static void byteBufSetGet() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        System.out.println((char) buf.getByte(0));
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        buf.setByte(0, (byte) 'B');
        System.out.println((char) buf.getByte(0));
        System.out.println(readerIndex == buf.readerIndex());
        System.out.println(writerIndex == buf.writerIndex());
    }
}
