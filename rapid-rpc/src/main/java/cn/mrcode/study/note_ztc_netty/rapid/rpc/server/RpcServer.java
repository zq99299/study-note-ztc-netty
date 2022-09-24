package cn.mrcode.study.note_ztc_netty.rapid.rpc.server;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcDecoder;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcEncoder;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcResponse;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.config.provider.ProviderConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/9/22 22:33
 */
@Slf4j
public class RpcServer {
    private String serverAddress;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * 服务启动
     *
     * @throws InterruptedException
     */
    private void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // tpc = sync + accept = backlog
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(
                                        // 数据包最大长度
                                        65535,
                                        // 长度字段的偏移量
                                        0,
                                        // 长度字段的长度
                                        4,
                                        // 添加到长度字段值的补偿
                                        0,
                                        // 从解码帧中剥离的第一个字节数
                                        0
                                ))
                                // 这里与客户端刚好相反
                                .addLast(new RpcEncoder(RpcResponse.class))
                                .addLast(new RpcDecoder(RpcRequest.class))
                                .addLast(new RpcServerHandler());
                    }
                });

        String[] items = serverAddress.split(":");
        String host = items[0];
        int port = Integer.parseInt(items[1]);
        // 同步等待完成
        ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("服务端绑定成功 ip={},port={}", host, port);
            } else {
                log.error("服务端绑定成功 ip={},port={}", host, port);
                throw new RuntimeException(future.cause());
            }
        });

        // 这里是同步等待 bind 完成，上面是异步的监听
        // 等待 5000
        channelFuture.await(5000, TimeUnit.MILLISECONDS);
    }

    private volatile Map<String, Object> handlerMap = new HashMap<>();

    /**
     * 程序注册器
     */
    public void registerProcessor(ProviderConfig providerConfig) {
        // key: userService 接口
        // value : userService 接口下的具体实现类实例对象
        handlerMap.put(providerConfig.getInterfaceClass(), providerConfig.getRef());
    }

    /**
     * 释放资源
     */
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
