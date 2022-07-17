package cn.mrcode.study.netty.test.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * @author mrcode
 * @date 2022/7/17 20:50
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 8086;
        new Thread(new TimeClientHandler("127.0.0.1", port), "Aio-001").start();
    }
}

class TimeClientHandler implements Runnable, CompletionHandler<Void, TimeClientHandler> {
    private AsynchronousSocketChannel asc;
    private String host;
    private int port;
    private CountDownLatch latch;

    public TimeClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            asc = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        asc.connect(new InetSocketAddress(host, port), this, this);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            asc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, TimeClientHandler attachment) {
        try {
            // 发送，也可以写容错，判断是否发送完成，半包的问题。这里就不处理半包问题了
            attachment.asc.write(ByteBuffer.wrap("你好呀".getBytes("UTF-8")));
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            // 异步读取操作，所以还是需要回调函数 处理读到的数据
            asc.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    attachment.flip();
                    byte[] body = new byte[attachment.remaining()];
                    attachment.get(body);
                    try {
                        System.out.println("== 收到消息：" + new String(body, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, TimeClientHandler attachment) {
        try {
            asc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

