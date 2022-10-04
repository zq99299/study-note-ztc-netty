package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/10/3 22:24
 */
public class RpcProxyImpl<T> implements InvocationHandler {
    // 代理的目标接口
    private Class<T> clazz;
    private String appName;
    private RpcClientManager rpcClientManager;

    public RpcProxyImpl(String appName, Class<T> clazz, RpcClientManager rpcClientManager) {
        this.appName = appName;
        this.clazz = clazz;
        this.rpcClientManager = rpcClientManager;
    }

    /**
     * @param proxy  调用该方法的代理实例
     * @param method 实例对应于代理实例上调用的接口方法。
     *               Method 对象的声明类将是声明该方法的接口，该接口可能是代理类继承该方法的代理接口的超接口。
     * @param args   一个对象数组，其中包含在代理实例上的方法调用中传递的参数值，如果接口方法不接受任何参数，则 null 。
     *               原始类型的参数被包装在适当的原始包装类的实例中，例如 java.lang.Integer 或 java.lang.Boolean 。
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 因为要模拟 helloService.hello("张山") 这样的调用方式，所以不用额外的去做什么异步调用
        // 异步调用交给客户端去做就好，或则以另外一种合适的方式来拓展

        // 1. 设置请求对象
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        //  request.setClassName(method.getDeclaringClass().getName());
        request.setClassName(clazz.getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        // 2. 选择一个合适的 client
        RpcClient rpcClient = rpcClientManager.chooseClient(appName);

        // 3. 发送真正的客户端请求，返回结果
        RpcFuture rpcFuture = rpcClient.sendRequest(request);
        Object result = rpcFuture.get();
        return result;
    }
}
