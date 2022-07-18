package cn.mrcode.study.netty.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;

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
    Console.log("服务启动成功，监听端口 {}", port);
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
          // 注册读写事件, 并且为每个 channel 注册一个处理程序
          channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Handler(channel));
        } else if (key.isReadable()) {
          ((Handler) key.attachment()).read(key);
        } else if (key.isWritable()) {
          ((Handler) key.attachment()).checkWrite();
        }
      }
      keys.clear();
    }
  }

  /**
   * 为每个 连接 处理的事件对象
   */
  static class Handler {
    private SocketChannel channel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    private Queue<String> writeQueue = new ArrayBlockingQueue(100);

    public Handler(SocketChannel channel) {
      this.channel = channel;
    }

    public void read(SelectionKey key) throws IOException {
      // 假定每次读取消息都读取到了整包，直接清空 buffer
      readBuffer.clear();
      int readBytes = channel.read(readBuffer);
      if (readBytes == -1) {
        channel.close();
        key.cancel();
        System.out.println("客户端已关闭");
        return;
      }
      // 上面写了 buffer，这里要读取的话，就需要翻转
      readBuffer.flip();
      byte[] bytes = new byte[readBuffer.remaining()];
      readBuffer.get(bytes);
      String body = new String(bytes, StandardCharsets.UTF_8);
      Console.log("[{}]收到信息：{}", DateUtil.formatDateTime(new Date()), body);

      // 收到一个消息，就发送一个当前时间
      final String message = DateUtil.formatDateTime(new Date());
      writeQueue.offer(message);
    }

    public void checkWrite() throws IOException {
      final String msg = writeQueue.poll();
      if (msg != null) {
        writeBuffer.clear();
        writeBuffer.put(msg.getBytes(StandardCharsets.UTF_8));
        writeBuffer.flip();  // 我们写完之后翻转，使用 channel 的时候才能读取到数据
        channel.write(writeBuffer);
      }
    }
  }
}
