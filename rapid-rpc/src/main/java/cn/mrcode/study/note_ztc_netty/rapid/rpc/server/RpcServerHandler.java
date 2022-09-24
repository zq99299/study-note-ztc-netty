package cn.mrcode.study.note_ztc_netty.rapid.rpc.server;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/9/22 22:39
 */
// 这里的泛型指的是入站，该类也是 inbound 所以对于服务端来说就是 RpcRequest
// 相反，对于客户端来说这里就是 RpcResponse
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Map<String, Object> handlerMap;
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 600L,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(65535));

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        /*
         1. 解析 rpcRequest
         2. 从 handlerMap 中扎到具体接口 所绑定的实例
         3. 通过反射 cglib 调用具体方法，传递相关执行参数，执行逻辑即可
         4. 返回响应信息
         */
        executor.submit(() -> {
            RpcResponse response = new RpcResponse();
            response.setRequestId(msg.getRequestId());
            try {
                Object result = handle(msg);
                response.setResult(result);
            } catch (Throwable t) {
                response.setThrowable(t);
                log.error("处理器执行异常：" + t);
            }
            ctx.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // 后置处理器
                }
            });
        });
    }

    /**
     * 解析 request 去反射调用本地具体服务
     *
     * @param msg
     * @return
     * @throws InvocationTargetException
     */
    private Object handle(RpcRequest msg) throws InvocationTargetException {
        String className = msg.getClassName();
        // 得到具体的服务实现实例
        Object serviceRef = handlerMap.get(className);
        Class<?> serviceClass = serviceRef.getClass();

        String methodName = msg.getMethodName();
        Class<?>[] parameterTypes = msg.getParameterTypes();
        Object[] parameters = msg.getParameters();

        // JDK relect 反射调用

        // 还可以使用 Cglib
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        Object result = serviceFastMethod.invoke(serviceRef, parameters);
        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("服务异常：" + cause);
        ctx.close();
    }

}
