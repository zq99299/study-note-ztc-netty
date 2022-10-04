package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.consumer.test;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client.RpcClient;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client.RpcClientConfig;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client.RpcClientManager;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.HelloService;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.provider.test.HelloServiceImpl;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.ArrayList;
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
}
