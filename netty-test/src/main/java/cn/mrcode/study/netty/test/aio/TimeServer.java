package cn.mrcode.study.netty.test.aio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class TimeServer {
    public static void main(String[] args) {
        int port = 8086;
        new Thread(new TimeServerHandler(port)).start();
    }
}

class TimeServerHandler implements Runnable {
    private int port;
    private CountDownLatch latch;
    AsynchronousServerSocketChannel ass;

    public TimeServerHandler(int port) {
        this.port = port;
        try {
            ass = AsynchronousServerSocketChannel.open();
            ass.bind(new InetSocketAddress(port));
            System.out.println("=== 服务器初始化成功.port = " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        doAccept(); //处理客户端链接
        try {
            latch.await(); // 不使用死循环的方式来阻止线程结束，使用发令枪的方法更优雅
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doAccept() {
        ass.accept(this, new CompletionHandler<AsynchronousSocketChannel, TimeServerHandler>() {
            @Override
            public void completed(AsynchronousSocketChannel result, TimeServerHandler attachment) {
                // 接受下一个链接
                attachment.ass.accept(attachment, this);
                // 获得了该链接，就可以处理该链接
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // 读取数据到 buffer 中、业务对象传递给回调处理中、通知回调
                result.read(buffer, buffer, new ReadCompletionHandler(result));
            }

            @Override
            public void failed(Throwable exc, TimeServerHandler attachment) {
                exc.printStackTrace();
                // 链接失败 发令枪发令，结束当前线程
                attachment.latch.countDown();
            }
        });
    }
}

class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
    private AsynchronousSocketChannel channel;

    public ReadCompletionHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] body = new byte[attachment.remaining()];
        attachment.get(body);
        String req = new String(body, Charset.forName("UTF-8"));
        System.out.println("=== 收到消息：" + req);
        // 响应消息
        doWrite("当前时间:" + new Date());
    }

    private void doWrite(String res) {
        byte[] bytes = res.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                // 如果没有发送完成，继续发送
                if (attachment.hasRemaining()) {
                    channel.write(attachment, attachment, this);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    channel.close();
                    exc.printStackTrace();
                    System.out.println("== 发送失败退出");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
