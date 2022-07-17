package cn.mrcode.study.netty.test.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * @author mrcode
 * @date 2022/7/17 15:45
 */
public class TimeServer {
    public static void main(String[] args) throws IOException {
        int port = 8086;
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);

            System.out.println("服务已启动，端口：" + port);
            Socket socket = null;
            while (true) {
                socket = server.accept();  // 通道链接上后，不手动关闭就会一直存在（长连接）
                Socket finalSocket = socket;
                // 新开一个线程处理请求
                new Thread(() -> {
                    try {
                        // 使用包装流读取和写出信息
                        BufferedReader in = new BufferedReader(new InputStreamReader(finalSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(finalSocket.getOutputStream()), true);
                        String body = null;
                        while (true) {
                            // 通过包装后，可以一行一行读取，如果下一行没有数据，则会阻塞，直到有数据为止
                            body = in.readLine();
                            System.out.println("== 收到信息：" + body);
                            if (body == null) {
                                break;
                            }
                            System.out.println("当前时间：" + new Date());
                            // 数据写出，通过包装后，一行一行的写，必须换行，否则消息不会发出
                            out.println("当前时间：" + new Date());
                        }
                        System.out.println("通信结束");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            finalSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } finally {
            if (server != null) {
                server.close();
            }
        }
    }
}
