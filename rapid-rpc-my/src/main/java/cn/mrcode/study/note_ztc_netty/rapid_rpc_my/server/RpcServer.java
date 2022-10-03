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
    private String serverAddress;

    // 两个 group 由于需要释放资源，所以提升为成员
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workGroup = new NioEventLoopGroup();

    /**
     * @param serverAddress ip:port，监听 ip 地址和端口，一般是 127.0.0.1:8765
     */
    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * 启动服务
     *
     * @param isSync 是否同步等待，如果为 true，该方法会一直阻塞，直到服务端断开关闭服务
     */
    public void start(boolean isSync) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 这里仅仅只是添加一个业务处理器： ServerHandler
                        ch.pipeline()
                                // 自定义协议的编码器，服务端对 RpcResponse 编码，因为服务端只响应 RpcResponse
                                .addLast(new RpcEncoder(RpcResponse.class))
                                // 简单的长度头 + 对应长度的消息体协议解码器
                                // 有值参数含义：一条消息的最大长度、消息头占用几个字节
                                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                // 自定义协议的解码器，服务端对 RpcRequest 解码，因为服务端只接受 RpcRequest
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

        String[] items = serverAddress.split(":");
        String host = items[0];
        int port = Integer.parseInt(items[1]);

        try {
            ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
            log.info("服务端启动成功，host={},port={}", host, port);
            if (isSync) {
                ChannelFuture closeFuture = channelFuture.channel().closeFuture();
                // 添加一个监听器
                closeFuture.addListener((ChannelFutureListener) future -> {
                    log.info("服务端已关闭服务");
                });
                closeFuture.sync();
            }
        } catch (Exception e) {
            log.error("服务端启动失败，host={},port={}", host, port);
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭服务
     */
    public void close() {
        // 具有合理值的 优雅的关闭方式
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
