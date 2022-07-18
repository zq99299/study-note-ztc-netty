package cn.mrcode.study.netty.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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
          channel.write(ByteBuffer.wrap("你好".getBytes(StandardCharsets.UTF_8)));
          // 然后关注读事件
          key.interestOps(SelectionKey.OP_READ);
        } else if (key.isReadable()) {
          // 假定每次都读取到了整包数据，不考虑半包，直接清空 buffer，重新读取
          buffer.clear();
          int readBytes = channel.read(buffer);
          if (readBytes == -1) {
            channel.close();
            key.cancel();
            System.out.println("客户端已关闭");
          } else {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String body = new String(bytes, StandardCharsets.UTF_8);
            System.out.println("收到信息：" + body);

            // 发送一条数据
            channel.write(ByteBuffer.wrap("x".getBytes(StandardCharsets.UTF_8)));
            TimeUnit.SECONDS.sleep(5);
          }
        }
      }
      // 处理完成后，要删除当次的 keys，否词下次还会继续消费到
      keys.clear();
    }
  }
}
