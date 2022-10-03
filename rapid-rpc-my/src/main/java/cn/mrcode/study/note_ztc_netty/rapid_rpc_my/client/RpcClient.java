package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcDecoder;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcEncoder;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
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
    private NioEventLoopGroup workGroup = new NioEventLoopGroup();

    private String serverAddress;

    private Channel channel;

    /**
     * @param serverAddress 要连接到哪一个服务端
     */
    public RpcClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }


    /**
     * 启动服务
     *
     * @param isSync 是否同步等待，如果为 true，该方法会一直阻塞，直到服务端断开关闭服务
     */
    public void start(boolean isSync) {
        Bootstrap bootstrap = new Bootstrap()
                .group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                // 这里的编解码器的对象和 服务端的相反
                                .addLast(new RpcEncoder(RpcRequest.class))
                                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                .addLast(new RpcDecoder(RpcResponse.class))
                                .addLast(new SimpleChannelInboundHandler<RpcResponse>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
                                        log.info("接收到响应消息：{}", msg);
                                    }
                                });
                    }
                });

        String[] items = serverAddress.split(":");
        String host = items[0];
        int port = Integer.parseInt(items[1]);
        try {
            // 链接服务端，并等待完成,
            // syncUninterruptibly 方法不抛出中断异常，而 sync 方法会抛出一个异常
            // 注意：这里不仅仅是只会抛出中断异常，比如服务端没有启动，会抛出 io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: /0.0.0.0:8765
            ChannelFuture channelFuture = bootstrap.connect(host, port).syncUninterruptibly();
            log.info("连接服务成功，host={},port={}", host, port);

            // 由于客户端需要发布信息，所以需要持有 channel
            this.channel = channelFuture.channel();

            if (isSync) {
                ChannelFuture closeFuture = channel.closeFuture();
                // 添加一个监听器
                closeFuture.addListener((ChannelFutureListener) future -> {
                    log.info("服务端已关闭服务");
                });
                closeFuture.sync();
            }
        } catch (Exception e) {
            log.error("连接到服务器失败：host={},port={}", host, port);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送请求
     */
    public void sendRequest() {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setMethodName("hello");
        rpcRequest.setParameters(new Object[]{"张三"});
        // rpcRequest.setParameterTypes(new Class[]{HelloService.class});
        // 发送一条数据到服务端
        channel.writeAndFlush(rpcRequest);
    }

    public void close() {
        workGroup.shutdownGracefully();
    }
}
