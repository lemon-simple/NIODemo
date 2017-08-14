package com.zs.io.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IOClient {
    private static final int port = 9091;

    private static final int ClientNumber = 3;

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        ExecutorService ex = Executors.newFixedThreadPool(ClientNumber);
        for (int i = 0; i < ClientNumber; i++) {
            ex.execute(getTask());
        }
    }

    /**
     * @throws IOException
     * @throws InterruptedException
     */
    private static Runnable getTask() {
        return new Runnable() {
            @Override
            public void run() {
                Socket client = new Socket();
                try {
                    client.connect(new InetSocketAddress("localhost", port));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("client connected" + client);
                int i = 0;
                String msg = null;
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                        msg = reader.readLine();
                        TimeUnit.SECONDS.sleep(5);
                        client.close();
                        System.out.println("i" + i++ + "msg[" + msg + "]");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}