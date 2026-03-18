package com.spoonb.day4.noworker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TaskServer {

    private static final AtomicInteger CONN_COUNT = new AtomicInteger(0);
    private static final AtomicLong REQ_COUNT = new AtomicLong(0);

    private static final Deque<byte[]> LEAK = new ArrayDeque<>();
    private static final int MAX_SIZE = 10;

    public static void main(String[] args) throws IOException {
        int port = 9090;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Listening on port " + port);

            while (true) {
                // 怎么等待
                Socket socket = serverSocket.accept();
                int current = CONN_COUNT.incrementAndGet();
                System.out.println("[Server] Accepted: " + socket.getRemoteSocketAddress()
                        + " | connection count =" + current);

                Thread t = new Thread(new ConnectionHandler(socket), "conn-" + current);
                t.start();
            }
        }
    }

    static class ConnectionHandler implements Runnable {
        private final Socket socket;

        ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Socket s = socket;
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {

                System.out.println(String.format("%s ===== start", Thread.currentThread().getName()));

                String line;
                while ((line = in.readLine()) != null) {
                    long n = REQ_COUNT.incrementAndGet();

                    if ("sleep 2000".equals(line)) {
                        Thread.sleep(2000);
                    }

                    // 每次响应写一行，并 flush，保证客户端能及时读到
                    out.write(line);
                    out.write("\n");
                    out.flush();

                    if (n % 1000 == 0) {
                        System.out.println("[Server] totalRequests=" + n);
                    }
                }

//                synchronized (LEAK) {
//                    if (LEAK.size() >= MAX_SIZE) { // 拒绝
//                        System.out.println("Server is busy!!!");
//                    } else {
//                        LEAK.push(new byte[1024 * 1024 * 10]);
//                    }
//                }

                System.out.println(String.format("%s ===== end", Thread.currentThread().getName()));

            } catch (IOException | InterruptedException e) {
                // 连接断开常见，打印简要信息即可
                System.out.println("[Server] Connection closed: " + e.getMessage());
            } finally {
                int current = CONN_COUNT.decrementAndGet();
                System.out.println("[Server] Disconnected | connections=" + current);
            }
        }
    }
}
