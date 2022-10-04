package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author mrcode
 * @date 2022/10/3 18:42
 */
@Slf4j
public class RpcClientManager {
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            2, // 核心线程
            10, // 最大线程，最多同时支持 n 个客户端连接
            60L,
            TimeUnit.SECONDS, // 60 秒后，空闲线程销毁，但是会保留 2 个核心线程等待
            new ArrayBlockingQueue<>(100) // 最多允许 100 个连接排队等待处理
    );
    /**
     * 存储服务名（appName） 与对应创建的连接列表
     */
    private Map<String, List<RpcClient>> cache = new ConcurrentHashMap<>();

    /**
     * 连接服务端
     *
     * @param list 连接到多个服务端
     */
    public void start(List<RpcClientConfig> list) {
        // 由于要连接多个服务端，这里会使用线程池去异步连接
        for (RpcClientConfig config : list) {
            threadPoolExecutor.submit(() -> {
                try {
                    RpcClient rpcClient = new RpcClient(config);
                    rpcClient.start(false);

                    String appName = config.getAppName();
                    List<RpcClient> rpcClients = cache.get(appName);
                    // 如果为空，则初始化一个集合
                    if (rpcClients == null) {
                        // 保证在并发下，不会覆盖 value
                        CopyOnWriteArrayList value = new CopyOnWriteArrayList();
                        rpcClients = cache.putIfAbsent(appName, value);
                        if (rpcClients == null) {
                            rpcClients = value;
                        }
                    }
                    rpcClients.add(rpcClient);
                } catch (Exception e) {
                    log.error("连接失败", e);
                }
            });
        }
    }

    /**
     * 停止服务, 停止所有已连接的服务
     */
    public void stop() {
        for (List<RpcClient> list : cache.values()) {
            for (RpcClient rpcClient : list) {
                rpcClient.close();
            }
        }
    }

    /**
     * 选择一个可用的服务的 rpcClient
     *
     * @param appName
     * @return 如果没有可用的连接，则返回 null
     */
    public RpcClient chooseClient(String appName) {
        /**
         * 选择的策略（其实就是 loadbalancer 负载均衡）
         * 比如 ribbon 的负载均衡策略 https://www.yuque.com/mrcode.cn/note-ztc/qzqxg1
         */

        // TOTO：这里先使用用一个随机选择的策略，后续可考虑完善这里的选择策略
        List<RpcClient> rpcClients = cache.get(appName);
        if (rpcClients == null) {
            return null;
        }
        if (rpcClients.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(rpcClients.size());
        return rpcClients.get(index);
    }


    /**
     * 用于存放对应服务的接口代理实现
     */
    private Map<String /* appName */, Map<Class /* 接口 class */, Object /* 该接口对应的代理 */>> proxyCache = new ConcurrentHashMap<>();

    /**
     * 获取接口代理对象
     *
     * @param interfaceClass 要调用远程的哪一个类
     * @param <T>
     * @return
     */
    public <T> T getProxy(String appName, Class<T> interfaceClass) {
        Map<Class, Object> proxyMap = proxyCache.get(appName);
        if (proxyMap == null) {
            ConcurrentHashMap<Class, Object> newMap = new ConcurrentHashMap<>();
            Map<Class, Object> old = proxyCache.putIfAbsent(appName, newMap);
            // 表示是新增的 map
            if (old == null) {
                proxyMap = newMap;
            } else {
                // 如果存在，表示其他线程已经设置过了
                proxyMap = old;
            }
        }

        Object cacheProxy = proxyMap.get(interfaceClass);
        if (cacheProxy != null) {
            return (T) cacheProxy;
        }

        // jdk 代理
        Object proxy = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new RpcProxyImpl<>(appName, interfaceClass, this)
        );
        proxyMap.putIfAbsent(interfaceClass, proxy);
        return (T) proxy;
    }
}
