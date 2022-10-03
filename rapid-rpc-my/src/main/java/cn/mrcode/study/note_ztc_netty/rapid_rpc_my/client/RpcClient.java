package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcDecoder;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcEncoder;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author mrcode
 * @date 2022/10/3 12:13
 */
@Slf4j
public class RpcClient {
    public static void start() throws InterruptedException {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap()
                .group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 添加客户端业务处理类
                        ch.pipeline()
                                .addLast(new RpcEncoder(RpcRequest.class))
                                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                // 这里与服务端刚好相反
                                .addLast(new RpcDecoder(RpcResponse.class))
                                .addLast(new SimpleChannelInboundHandler<RpcResponse>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
                                        log.info("接收到响应消息：{}", msg);
                                    }
                                });
                    }
                });

        // 链接服务端，并等待完成
        ChannelFuture channelFuture = bootstrap.connect("localhost", 8765).syncUninterruptibly();

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setMethodName("hello");
        rpcRequest.setParameters(new Object[]{"张三"});
//        rpcRequest.setParameterTypes(new Class[]{HelloService.class});
        // 发送一条数据到服务端
        channelFuture.channel().writeAndFlush(rpcRequest);

        // 等待服务端的响应
//        TimeUnit.SECONDS.sleep(5);
        // 同步阻塞关闭监听器，等待监听器关闭完成
        channelFuture.channel().closeFuture().sync();
        workGroup.shutdownGracefully();
    }
}
