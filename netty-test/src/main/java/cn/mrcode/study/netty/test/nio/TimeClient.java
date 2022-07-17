package cn.mrcode.study.netty.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/7/17 18:50
 */
public class TimeClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8086;
        String host = "localhost";
        Selector selector = Selector.open();
        SocketChannel channel = SocketChannel.open();
        //设置非阻塞
        channel.configureBlocking(false);
        // 设置关注连接事件，并且注册到选择器中
        channel.register(selector, SelectionKey.OP_CONNECT);
        // 发起链接
        channel.connect(new InetSocketAddress(host, port));

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                if (key.isConnectable()) {
                    channel.finishConnect();
                    // 链接成功，发送一个问候消息
                    channel.write(ByteBuffer.wrap("你好".getBytes()));
                    // 然后关注读写事件
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else if (key.isReadable() && key.isValid()) {
                    int readBytes = channel.read(buffer);
                    if (readBytes == -1) {
                        channel.close();
                        key.cancel();
                        System.out.println("客户端已关闭");
                    } else {
                        // 将 byteBuffer 的 position 和 limit 设置为 0，以便读取数据
                        buffer.flip();
                        // 读取数据
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        String body = new String(bytes, "UTF-8");
                        System.out.println("收到信息：" + body);
                        buffer.clear();

                        channel.write(ByteBuffer.wrap("x".getBytes()));
                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                        TimeUnit.SECONDS.sleep(5);
                    }
                }
            }
            // 处理万抽后一定要清空这次的事件，否则下次还会重复消费当次事件
            keys.clear();
        }
    }
}
