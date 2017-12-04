package com.zs.io.cs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author zhangsh
 *
 */
public class IOServer {

    public static void main(String[] args) {

        ExecutorService ex = Executors.newFixedThreadPool(10);
        int i = 0;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9091);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();// 阻塞等待,直到有客户端接入

                System.out.println("new Client connected :[" + clientSocket + "]" + i++);

                ex.execute(getTask(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Runnable getTask(final Socket clientSocket) {
        return new Runnable() {

            @Override
            public void run() {
                assmbleTaskWithClient(clientSocket);
            }
        };
    }

    /**
     * @param clientSocket
     * @throws IOException
     */
    private static void assmbleTaskWithClient(Socket clientSocket) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String lineStr;
            writer.write("hi,client! \r\n");
            writer.flush();

            StringBuffer responseStr = new StringBuffer();
            while (null != (lineStr = reader.readLine())) {
                responseStr.append(lineStr);
                System.out.println("received from client:[" + clientSocket + "],msg:[" + responseStr.toString() + "]");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}