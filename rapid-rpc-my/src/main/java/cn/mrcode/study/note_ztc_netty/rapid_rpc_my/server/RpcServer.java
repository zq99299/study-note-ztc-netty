package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.server;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcDecoder;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcEncoder;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mrcode
 * @date 2022/10/3 12:12
 */
@Slf4j
public class RpcServer {
    public static void start() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 这里仅仅只是添加一个业务处理器： ServerHandler
                        ch.pipeline()
                                .addLast(new RpcEncoder(RpcResponse.class))
                                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                // 这里与客户端刚好相反
                                .addLast(new RpcDecoder(RpcRequest.class))
                                .addLast(new SimpleChannelInboundHandler<RpcRequest>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
                                        log.info("接收消息：{}", msg);
                                        RpcResponse response = new RpcResponse();
                                        response.setRequestId(msg.getRequestId());
                                        response.setResult("调用成功");
                                        ctx.writeAndFlush(response);
                                    }
                                });
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
