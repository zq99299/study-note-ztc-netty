package cn.mrcode.study.netty.test.marshalling;

import cn.hutool.core.lang.Console;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

/**
 * @author mrcode
 * @date 2022/7/30 22:26
 */
public class Client {
    public static void main(String[] args) throws InterruptedException {
        String host = "localhost";
        int port = 8086;

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
                            ch.pipeline()
                                    // 设置解码器和编码器
                                    .addLast(MarshallingCodeCFactory.buildMarshallingDecoder())
                                    .addLast(MarshallingCodeCFactory.buildMarshallingEncoder())
                                    // 最后设置自己的业务处理器，消息读写
                                    .addLast(new ClientHandler());
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


    private static class ClientHandler extends ChannelInboundHandlerAdapter {

        /**
         * 客户端和服务端 TCP 链路建立成功之后，该方法被调用
         *
         * @param ctx
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            for (int i = 0; i < 10; i++) {
                RequestData req = new RequestData();
                req.setId(i + "");
                req.setName("我是消息：" + i);
                req.setRequestMessage("内容: " + i);
                // 这里附件你可以读取一个文件，然后发送，这里就直接写字符串了
                req.setAttachment(("附件: " + i).getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(req);
            }
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
            ResponseData body = (ResponseData) msg;
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
