package cn.mrcode.study.netty.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author mrcode
 * @date 2022/7/17 16:50
 */
public class TimeServer {
    public static void main(String[] args) throws IOException {
        int port = 8086;

        // 初始化选择器
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        // 设置非阻塞
        serverSocketChannel.configureBlocking(false);
        // 注册自己感兴趣的事件，这里只对接受连接的事件感兴趣
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            // 使用阻塞的选择操作，有事件才会返回，否则阻塞
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                if (key.isAcceptable()) {
                    // 创建链接
                    SocketChannel channel = serverSocketChannel.accept();
                    // 设置非阻塞
                    channel.configureBlocking(false);
                    // 注册读写事件 并设置一个 byteBuffer 到 channel 对应的选择器中
                    // 每个 channel 分配一个 byteBuffer 用来读写
                    channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                } else if (key.isReadable() && key.isValid()) {
                    // 读取数据
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
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
                        String currentTime = "当前时间：" + new Date();
                        System.out.println("发送当前时间");
                        buffer.put(currentTime.getBytes());
                        buffer.flip();
                        channel.write(buffer);
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
            keys.clear();
        }
    }
}
