package cn.mrcode.study.netty.test.helloword;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author mrcode
 * @date 2022/7/18 21:13
 */
public class Server {
    public static void main(String[] args) throws InterruptedException {
        // 创建两个线程组：一个用于接收客户端的连接，一个用于处理客户端的请求
        // NioEventLoopGroup 是一个处理 I/O 操作的多线程事件循环。Netty 为不同类型的传输提供了各种 EventLoopGroup 实现。
        // 在本例中，我们正在实现一个服务器端应用程序，因此将使用两个 NioEventLoopGroup。
        // 第一个，通常被称为 老板（boss），接受传入的连接。
        // 第二个，通常称为 worker，在老板接受连接后处理已接受的连接的流量，并将已接受的连接注册到 work。
        // 使用多少线程以及它们如何映射到创建的通道取决于 EventLoopGroup 实现，甚至可以通过构造函数进行配置。
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        // 创建一个 ServerBootstrap 对象，用于配置服务端的 NIO 的工作线程
        // ServerBootstrap 是一个设置服务器的帮助类。您可以直接使用 Channel 设置服务器。
        // 但是，请注意，这是一个乏味的过程，大多数情况下您不需要这样做。
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                // server 端：所以需要设置 NioServerSocketChannel
                // 这里，我们指定使用 NioServerSocketChannel 类，该类用于实例化一个新的 Channel 来接受传入的连接。
                .channel(NioServerSocketChannel.class)
                // 设置链接超时时间，单位毫秒
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                // 设置 tcp backlog 参数 = sync 队列 + accept 对象
                // 您还可以设置特定于 Channel 实现的参数。我们正在编写一个 TCP/IP 服务器，因此允许我们设置套接字选项，如 tcpNoDelay 和 keepAlive。
                // 请参考 ChannelOption 的 apidocs 和具体的 ChannelConfig 实现来获得关于支持的 ChannelOptions 的概述。
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 你注意到 option() 和 childOption() 了吗?
                // option() 用于接收传入连接的 NioServerSocketChannel。
                // childOption() 用于父 ServerChannel 接受的通道，在本例中为 NioSocketChannel。
                // 设置配置项 通信不延迟
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 设置配置项 接受与发送缓存区大小
                .childOption(ChannelOption.SO_RCVBUF, 1024 * 32)
                .childOption(ChannelOption.SO_SNDBUF, 1024 * 32)
                // 初始化 ChannelInitializer ，用于构建双向链表 pipeline 添加业务 handler 处理
                // 此处指定的处理程序将始终由新接受的 Channel 计算。
                // ChannelInitializer 是一个特殊的处理程序，用于帮助用户配置新的 Channel。
                // 您很可能希望通过添加一些处理程序(如 DiscardServerHandler)来配置新 Channel 的 ChannelPipeline，以实现您的网络应用程序。
                // 随着应用程序变得复杂，您可能会向管道添加更多的处理程序，并最终将这个匿名类提取到顶级类中。
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 这里仅仅只是添加一个业务处理器： ServerHandler
                        ch.pipeline().addLast(new ServerHandler());
                    }
                });
        // 绑定端口，同步等待成功
        // 使用 channel 级别的监听 close 端口，阻塞方式
        // 我们现在准备好了。剩下的工作就是绑定到端口并启动服务器。这里，我们绑定到机器中所有 nic(网络接口卡) 的 8765 端口。
        // 现在，您可以随时调用 bind() 方法(使用不同的绑定地址)。
        ChannelFuture channelFuture = bootstrap.bind(8765).sync();
        channelFuture.channel().closeFuture().sync();

        // 释放资源：关闭线程池，关闭 channel
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
