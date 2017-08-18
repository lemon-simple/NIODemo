package com.zs.nio.scatterAndGather;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhangsh
 */
public class NIOTransferData {

    public static void main(String[] args) throws IOException {
        System.out.println(1 << 2);
        transferData();
    }

    public static void transferData() throws IOException {
        RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
        FileChannel fromChannel = fromFile.getChannel();

        RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
        FileChannel toChannel = toFile.getChannel();

        long position = 0;
        long count = fromChannel.size();

        toChannel.transferFrom(fromChannel, position, count);

        RandomAccessFile toFileEnd = new RandomAccessFile("toFileEnd.txt", "rw");
        FileChannel toChannelEnd = toFileEnd.getChannel();
        toChannel.transferTo(position, toChannel.size(), toChannelEnd);
    }
}
