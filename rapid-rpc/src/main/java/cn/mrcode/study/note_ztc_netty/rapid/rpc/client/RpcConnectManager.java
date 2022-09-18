package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 连接管理器
 *
 * @author mrcode
 * @date 2022/9/18 17:42
 */
@Slf4j
public class RpcConnectManager {
    /*
       volatile 在多线程使用时，对象里面的内容可能会发生改变，使用线程可见性 volatile 来修饰
     */
    private static volatile RpcConnectManager RPC_CONNECT_MANAGER = new RpcConnectManager();

    // 做饥饿单例模式
    private RpcConnectManager() {
    }

    public static RpcConnectManager getInstance() {
        return RPC_CONNECT_MANAGER;
    }

    /**
     * 一个地址对应一个 client 处理器
     */
    private Map<InetSocketAddress, RpcClientHandler> connectedHandlerMap = new ConcurrentHashMap<>();
    /**
     * 用于异步提交连接的线程池
     */
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            5,  // 核心线程
            5, // 最大线程
            60L, // 线程释放空闲时间，这里是 60 + 后面的单位
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), // 允许排队的数量
            new ThreadPoolExecutor.DiscardPolicy() // 拒绝策略：DiscardPolicy 是直接丢弃掉被拒绝的任务
    );

    /*
      1. 异步连接：使用线程池真正发起连接，连接失败监听、连接成功监听
      2. 对于连接成功后，做一个缓存（管理）
     */

    /**
     * @param serverAddress 192.168.1.1:8888,192.168.1.2:8888
     */
    public void connect(final String serverAddress) {
        List<String> address = Arrays.asList(serverAddress.split(","));
        updateConnectedServer(address);
    }

    /**
     * 更新缓存信息，并异步发起连接
     *
     * @param address
     */
    public void updateConnectedServer(List<String> address) {
        if (CollectionUtils.isEmpty(address)) {
            log.error("没有可用的服务地址");
            return;
        }

        // 解析为 InetSocketAddress 集
        HashSet<InetSocketAddress> socketAddresses = new HashSet<>();
        for (String adders : address) {
            String[] items = adders.split(":");
            if (items.length == 2) {
                String host = items[0];
                int port = Integer.parseInt(items[1]);
                InetSocketAddress remoteIsa = new InetSocketAddress(host, port);
                socketAddresses.add(remoteIsa);
            }
        }

        // 发起连接
        for (InetSocketAddress socketAddress : socketAddresses) {
            // 如果已经存在，则不再发起连接
            if (connectedHandlerMap.containsKey(socketAddress)) {
                continue;
            }

            connectAsync(socketAddress);
        }

        // todo: 如果缓存中 connectedHandlerMap 存在 socketAddresses 没有的地址，需要清除该地址的相关资源
    }


    /**
     * 默认是 CPU x 2
     */
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    /**
     * 异步发起连接
     *
     * @param socketAddress
     */
    private void connectAsync(InetSocketAddress socketAddress) {
        threadPoolExecutor.submit(() -> {
            Bootstrap b = new Bootstrap();
            b
                    .group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    // 这里使用一个自定义的 ChannelInitializer 子类来实现相关 handler 的逻辑封装
                    .handler(new RpcClientInitializer());
            connect(b, socketAddress);
        });
    }

    private void connect(final Bootstrap b, InetSocketAddress socketAddress) {

    }
}
