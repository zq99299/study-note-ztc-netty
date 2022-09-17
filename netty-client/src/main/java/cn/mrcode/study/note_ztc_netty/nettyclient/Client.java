package cn.mrcode.study.note_ztc_netty.nettyclient;

import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.MessageModule;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/8/31 21:35
 */
@Component
public class Client {
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private EventLoopGroup group = new NioEventLoopGroup(2);
    public static final String HOST = "127.0.0.1";
    private static final int PORT = 8765;

    private Channel channel;

    public Client() throws InterruptedException {
        connect(HOST, PORT);
    }

    public void connect(String host, int port) throws InterruptedException {
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // varint32Frame 解码器
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    // 我们自定义的 Message 解码器
                                    .addLast(new ProtobufDecoder(MessageModule.Message.getDefaultInstance()))

                                    // varint32Frame 的编码器
                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                                    // 编码器
                                    .addLast(new ProtobufEncoder())

                                    // 最后是我们自己的业务处理器
                                    .addLast(new ClientHandler());
                        }
                    });
            ChannelFuture future = b.connect(host, port).sync();
            this.channel = future.channel();
            System.out.println("Client 链接成功...");
            this.channel.closeFuture().sync();
        } finally {
            // 所有资源释放完成之后，清空资源，再次发起重连操作
            // 因为 this.channel.closeFuture().sync(); 这里会阻塞，如果链接断开的话，就会往下走
            // 该方法就结束了，然后会走 finally 代码块，就会在一个线程里面去发起链接操作
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        connect(host, port);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
