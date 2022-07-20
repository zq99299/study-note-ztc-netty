package cn.mrcode.study.netty.test.nettytime;

import cn.hutool.core.lang.Console;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @author mrcode
 * @date 2022/7/19 22:12
 */
public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        int port = 8086;
        new TimeClient().connect("127.0.0.1", port);
    }

    public void connect(String host, int port) throws InterruptedException {
        // 配置客户端NIO线程组
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 通道建立后，绑定我们的处理类
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    });
            // 发起异步链接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 同步阻塞，链路关闭才被唤醒
            future.channel().closeFuture().sync();

        } finally {
            //优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    private class TimeClientHandler extends ChannelInboundHandlerAdapter {

        /**
         * 客户端和服务端 TCP 链路建立成功之后，该方法被调用
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            byte[] req = (Thread.currentThread().getName() + " 发起请求").getBytes(StandardCharsets.UTF_8);
            ByteBuf firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
            ctx.writeAndFlush(firstMessage);
        }

        /**
         * 服务端返回应答消息时，该方法被调用
         *
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req, "UTF-8");
            Console.log("== 接收到消息:{}", body);
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}