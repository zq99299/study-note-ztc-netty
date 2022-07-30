package cn.mrcode.study.netty.test.nettytime.delimit;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author mrcode
 * @date 2022/7/19 22:05
 */
public class TimeServer {

    public static void main(String[] args) throws InterruptedException {
        int port = 8086;
        new TimeServer().bind(port);
    }

    public void bind(int port) throws InterruptedException {
        // 配置服务端的NIO线程组
        // 包含一组NIO线程，专门用于网络事件的处理
        // 实际上他们就是Reactor线程组。

        //用于接收客户端的链接
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // 用于SocketChannel的网络读写
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        // 引导配置
        ServerBootstrap starp = new ServerBootstrap();
        try {
            starp.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定通道类型
                    .option(ChannelOption.SO_BACKLOG, 1024) // 缓存大小
                    .childHandler(new ChildChannelHandler()); // 绑定处理器
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

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline()
                    // 只需要更换这里的解码器即可
                    // 最大帧长度，如果超过此长度还没有找到后面的分隔符，则会抛出异常
                    // 这里的分隔符选择 $_, 意味着在发布消息的时候也要使用该分隔符
                    .addLast(new DelimiterBasedFrameDecoder(100, Unpooled.copiedBuffer("$_".getBytes())))
                    // 将 ByteBuf 中获取到的 byte 转换为 String
                    .addLast(new StringDecoder(StandardCharsets.UTF_8))
                    .addLast(new TimeServerHanler());
        }
    }


    private class TimeServerHanler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 需要注意的是：使用了 StringDecoder 之后，这里的消息就是 String 类型了
            String body = (String) msg;
            Console.log("== 请求消息：{}", body);

            String outMsg = DateUtil.formatDateTime(new Date()) + "$_";
            ByteBuf resp = Unpooled.copiedBuffer(outMsg.getBytes(StandardCharsets.UTF_8));
            ctx.write(resp); // 异步发送应答
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            // 将消息发送队列中的消息写入到 SocketChannel 中发送给对方
            // 性能考虑，放置频繁唤醒 Selector 进行消息发送。
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
