package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.consumer.test;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client.RpcClient;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.HelloService;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.provider.test.HelloServiceImpl;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;


/**
 * 调用者测试
 *
 * @author mrcode
 * @date 2022/9/28 22:01
 */
@Slf4j
public class ConsumerStarter {
    /**
     * 期望：服务端暴露服务
     */
    public void expectProvider() {
        HelloServiceImpl helloService = new HelloServiceImpl();
        // 有一个服务，期望暴露给客户端，客户端可以发起调用
    }

    /**
     * 期望：客户端调用服务
     */
    @Test
    public void expectConsumer() {
        // 我们最终的目的
        HelloService helloService = null;// ...通过某种方式获得这个服务实例;
        // 然后发起调用
        helloService.hello("张山");
    }

    @Test
    public void serverTest() {
        String serverAddress = "0.0.0.0:8765";
        RpcServer rpcServer = new RpcServer(serverAddress);

        // 使用线程来测试 5 秒后关闭服务，测试服务关闭的 ChannelFuture 添加的监听器是否能正常工作
//        new Thread(() -> {
//            try {
//                // 5 秒后关闭服务
//                TimeUnit.SECONDS.sleep(5);
//                rpcServer.close();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();

        // 启动服务，这里使用同步等待服务关闭，不然这个测试线程启动后就被关闭了
        rpcServer.start(true);
        log.info("服务已关闭");
    }

    @Test
    public void clientTest() throws InterruptedException {
        String serverAddress = "0.0.0.0:8765";
        RpcClient rpcClient = new RpcClient(serverAddress);
        rpcClient.start(false);

        rpcClient.sendRequest();

        // 发送完成之后，等待几秒，等待客户端的响应
        TimeUnit.SECONDS.sleep(5);
    }
}
