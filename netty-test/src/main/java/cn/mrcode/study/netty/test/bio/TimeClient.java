package cn.mrcode.study.netty.test.bio;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/7/17 15:49
 */
public class TimeClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8086;
        // 连接服务端
        Socket socket = new Socket("127.0.0.1", port);
        // 使用包装流读取和写出信息
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        out.println("你好");
        String body = null;
        while (true) {
            body = in.readLine();
            if (body == null) {
                break;
            }
            out.println("x");
            System.out.println("收到响应消息：" + body);
            TimeUnit.SECONDS.sleep(2);
        }
        System.out.println("通信结束");
    }
}
