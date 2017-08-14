package com.zs.io.cs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 1.ServerSocket 上的 accept()方法将会一直阻塞直到一个连接建立 ，随后返回一个 新的 Socket 用于客户端和服务器之间的通信。
 * 
 * 2. 该 ServerSocket 将继续监听请求的其他socket连接。
 * 
 */
public class IOServer {

    private static final int port = 9091;

    public static void main(String[] args) throws IOException, InterruptedException {

        ServerSocket serverSocket = new ServerSocket(port);
        ExecutorService ex = null;
        while (!Thread.currentThread().isInterrupted()) {
            Socket socket = serverSocket.accept();
            System.out.println("server connnected:   socket  accepted   " + socket);
            ex = Executors.newFixedThreadPool(10);
            ex.execute(getTask(socket));
        }
        ex.shutdown();
        serverSocket.close();
    }

    static Runnable getTask(Socket socket) {
        return new Runnable() {
            @Override
            public void run() {
                int i = 0;
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                        writer.write(i++ + "hello client \n");
                        writer.flush();
                        TimeUnit.SECONDS.sleep(6);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != writer)
                            writer.close();
                        if (null != socket)
                            socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}