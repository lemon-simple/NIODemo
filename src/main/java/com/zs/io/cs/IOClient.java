package com.zs.io.cs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IOClient {
    public static void main(String[] args) throws UnknownHostException, IOException {

        ExecutorService ex = Executors.newFixedThreadPool(10);

        Socket clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress("localhost", 9091));

        System.out.println("connected client:[" + clientSocket + "]");

        ex.execute(getTask(clientSocket));
    }

    private static Runnable getTask(Socket clientSocket) {
        return new Runnable() {

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
        try {
            // BufferedReader reader = new BufferedReader(new
            // InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            for (int i = 0; i < 10; i++) {
                writer.write("hi server! \r\n");
                writer.flush();

                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // String lineStr;
            // StringBuffer responseStr = new StringBuffer();
            //
            // while (null != (lineStr = reader.readLine())) {
            // responseStr.append(lineStr);
            // System.out.println("received from server:[" + clientSocket +
            // "],msg:[" + responseStr.toString() + "] ");
            // }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}