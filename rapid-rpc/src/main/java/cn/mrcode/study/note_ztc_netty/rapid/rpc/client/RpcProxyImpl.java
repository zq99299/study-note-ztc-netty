package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/9/26 22:11
 */
public class RpcProxyImpl<T> implements InvocationHandler, RpcAsyncProxy {
    private Class<T> clazz;
    private long timeout;

    public RpcProxyImpl(Class<T> clazz, long timeout) {
        this.clazz = clazz;
        this.timeout = timeout;
    }

    /**
     * 代理结构调用方式
     *
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
        // 1. 设置请求对象
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        // 2. 选择一个合适的 client 任务处理器
        RpcClientHandler rpcClientHandler = RpcConnectManager.getInstance().chooseHandler();

        // 3. 发送真正的客户端请求，返回结果
        RpcFuture rpcFuture = rpcClientHandler.sendRequest(request);
        Object result = rpcFuture.get(timeout, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public RpcFuture call(String functionName, Object... args) {
        // 1. 设置请求对象
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(clazz.getName());
        request.setMethodName(functionName);

        // 这里最安全的方式不能使用这种方法来获取
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Class<?> classType = getClassType(args[i]);
            parameterTypes[i] = classType;
        }
        request.setParameterTypes(parameterTypes);
        request.setParameters(args);

        RpcClientHandler rpcClientHandler = RpcConnectManager.getInstance().chooseHandler();
        RpcFuture rpcFuture = rpcClientHandler.sendRequest(request);
        return rpcFuture;
    }

    /**
     * 获取参数类型
     *
     * @param obj
     * @return
     */
    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        // 基本数据类型
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.Lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }
        // 如果没有就返回原始类型
        return classType;
    }
}