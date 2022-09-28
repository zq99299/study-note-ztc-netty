package cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.consumer.test;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.client.RpcAsyncProxy;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.client.RpcClient;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.client.RpcFuture;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.HelloService;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.User;

import java.util.concurrent.ExecutionException;

/**
 * 调用者测试
 *
 * @author mrcode
 * @date 2022/9/28 22:01
 */
public class ConsumerStarter {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        sync();
        // async();
    }

    /**
     * 同步调用
     */
    public static void sync() {
        RpcClient rpcClient = new RpcClient("127.0.0.1:8765", 3000);
        HelloService helloService = rpcClient.invokeSync(HelloService.class);
        String result = helloService.hello("张三");
        System.err.println(result);
    }

    public static void async() throws ExecutionException, InterruptedException {
        RpcClient rpcClient = new RpcClient("127.0.0.1:8765", 3000);
        RpcAsyncProxy proxy = rpcClient.invokeAsync(HelloService.class);
        RpcFuture future = proxy.call("hello", "李四");
        RpcFuture future2 = proxy.call("hello",
                User.builder().age(18).name("wang5").build());

        Object result = future.get();
        Object result2 = future2.get();
        System.err.println(result);
        System.err.println(result2);
    }
}
