package cn.mrcode.study.netty.test.marshalling;

import cn.hutool.core.lang.Console;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 服务端
 *
 * @author mrcode
 * @date 2022/7/30 22:04
 */
public class Server {
    public static void main(String[] args) throws InterruptedException {
        int port = 8086;
        // 用于接收客户端的链接: 只设置为单线程模式
        // 如果不设置，也没有其他的系统变量的话，那么就会使用 NettyRuntime.availableProcessors() * 2) 作为默认的线程数
        // 也就是 CPU 核数 x 2
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 用于 SocketChannel 的网络读写
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap starp = new ServerBootstrap();
        try {
            starp.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定通道类型
                    .option(ChannelOption.SO_BACKLOG, 1024) // 缓存大小
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 设置解码器和编码器
                                    .addLast(MarshallingCodeCFactory.buildMarshallingDecoder())
                                    .addLast(MarshallingCodeCFactory.buildMarshallingEncoder())
                                    // 最后设置自己的业务处理器，消息读写
                                    .addLast(new ServerHandler());
                        }
                    }); // 绑定处理器
            // 绑定端口，同步等待成功。 ChannelFuture： 类似 Jdk.Future, 用于异步操作的通知回调
            ChannelFuture channelFuture = starp.bind(port).sync();
            // 等待服务器监听端口关闭。该方法会阻塞，链路关闭后，会被唤醒
            channelFuture.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            RequestData body = (RequestData) msg;
            Console.log("== 请求消息：{}", body);

            ResponseData responseData = new ResponseData();
            responseData.setId(body.getId());
            responseData.setName(body.getName());
            responseData.setResponseMessage("服务端内容");
            ctx.write(responseData); // 异步发送应答
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            // 将消息发送队列中的消息写入到 SocketChannel 中发送给对方
            // 性能考虑，防止频繁唤醒 Selector 进行消息发送。
            // Netty 的方法并不直接将消息写入 SocketChannel 中
            // 调用 write 只是把消息放到了发送缓冲数组中。
            // 通过 flush 方法将缓冲区中的消息全部写入到 SocketChannel 中
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 发生异常，释放相关句柄资源
            ctx.close();
            cause.printStackTrace();
        }
    }
}

