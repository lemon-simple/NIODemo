/*
 * Copyright (C) 2014-2016 Omniprime All rights reserved
 * Author: zhangsh
 * Date: 2017年8月9日
 * Description:FileChannel.java 
 */
package com.zs.nio.channel;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhangsh
 *
 */
public class FileChannelTest {

    public static void main(String[] args) throws IOException {

        RandomAccessFile randomAccessFile = new RandomAccessFile("fileChannel.txt", "rw");

        FileChannel fileChannel = randomAccessFile.getChannel();
        System.out.println("fileChannel" + fileChannel.size());

        ByteBuffer byteBuffer = ByteBuffer.allocate(36);// 1kb

        fileChannel.read(byteBuffer);
        byteBuffer.flip();

        while (byteBuffer.hasRemaining()) {
            System.out.println((char) byteBuffer.get());
        }

        fileChannel.write(byteBuffer);

        fileChannel.force(true);

        byteBuffer.clear();
        randomAccessFile.close();
    }

}
