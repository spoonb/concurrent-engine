package com.spoonb.day2.obsolete;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadClient {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i ++) {
            Future<?> submit = executorService.submit(new TaskRunner());
            submit.get();
        }

        executorService.shutdown();
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
                    String cmd = "PING";

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
