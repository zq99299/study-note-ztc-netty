package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mrcode
 * @date 2022/9/19 21:50
 */
public class RpcClient {
    private String serverAddress;
    private long timeout;

    public RpcClient(String serverAddress, long timeout) {
        this.serverAddress = serverAddress;
        this.timeout = timeout;
        connect();
    }

    private void connect() {
        RpcConnectManager.getInstance().connect(serverAddress);
    }

    public void stop() {
        RpcConnectManager.getInstance().stop();
    }

    private final Map<Class<?>, Object> syncProxyIntanceMap = new ConcurrentHashMap<>();

    /**
     * 同步调用代理方法
     *
     * @param interfaceClass 要调用远程的哪一个类
     * @param <T>
     * @return
     */
    public <T> T invokeSync(Class<T> interfaceClass) {
        if (syncProxyIntanceMap.containsKey(interfaceClass)) {
            return (T) syncProxyIntanceMap.get(interfaceClass);
        }
        Object proxy = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new RpcProxyImpl<>(interfaceClass, timeout)
        );
        syncProxyIntanceMap.put(interfaceClass, proxy);
        return (T) proxy;
    }
}
