package cn.mrcode.study.note_ztc_netty.nettyserver;

import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.MessageModule;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.stereotype.Component;

/**
 * @author mrcode
 * @date 2022/9/6 21:16
 */
@Component
public class Server {
    public Server() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
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
                                    .addLast(new ServerHandler());
                        }
                    });
            ChannelFuture cf = b.bind(8765).sync();
            System.out.println("监听端口 8765");
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("Netty 服务关闭");
        }

    }
}
