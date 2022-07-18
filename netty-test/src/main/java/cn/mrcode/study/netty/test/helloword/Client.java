package cn.mrcode.study.netty.test.helloword;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/7/18 21:35
 */
public class Client {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap()
                .group(workGroup)
                .channel(NioSocketChannel.class)
                // 设置链接超时时间，单位毫秒
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                // 设置配置项 接受与发送缓存区大小
                .option(ChannelOption.SO_RCVBUF, 1024 * 32)
                .option(ChannelOption.SO_SNDBUF, 1024 * 32)
                // 初始化 ChannelInitializer
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 添加客户端业务处理类
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

        // 链接服务端，并等待完成
        ChannelFuture channelFuture = bootstrap.connect("localhost", 8765)
                .syncUninterruptibly();

        // 发送一条数据到服务端
        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello world".getBytes(StandardCharsets.UTF_8)));

        // 休眠 1 秒后再发送一条数据到 服务端
        TimeUnit.SECONDS.sleep(1);

        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello world again!".getBytes(StandardCharsets.UTF_8)));

        // 同步阻塞关闭监听器，等待监听器关闭完成
        channelFuture.channel().closeFuture().sync();
        workGroup.shutdownGracefully();
    }
}
