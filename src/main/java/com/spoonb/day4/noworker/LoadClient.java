package com.spoonb.day4.noworker;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class LoadClient {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        for (int i = 0; i < 1000; i ++) {
            new Thread(new TaskRunner(), String.format("thread-%s", i)).start();
        }
        Thread.sleep(1000000);

    }

    private static class TaskRunner implements Runnable {

        @Override
        public void run() {
            String host = "127.0.0.1";
            int port = 9090;
            int requests = 100;

            long startNs = System.nanoTime();

            try (Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

                for (int i = 1; i <= requests; i++) {
                    // 你可以改成 ECHO hello-i 看回显
                    Random random = new Random();
                    String cmd;

                    if (random.nextInt(100) < 5) {
                        cmd = "sleep 2000";
                    } else {
                        cmd = "PING";
                    }

                    out.write(cmd);
                    out.write("\n");
                    out.flush();

                    String resp = in.readLine();
                    if (resp == null) {
                        throw new IOException("Server closed connection unexpectedly");
                    }

                    System.out.println("[Client] resp#" + i + " = " + resp);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            long costMs = (System.nanoTime() - startNs) / 1_000_000;
            System.out.println("[Client] Done. requests=" + requests + ", costMs=" + costMs);
        }
    }
}
