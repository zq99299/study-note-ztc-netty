import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client.RpcClient;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client.RpcClientConfig;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client.RpcClientManager;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/10/3 11:46
 */
@Slf4j
public class Demo {

    /**
     * 多服务启动，模拟有多个微服务提供服务
     */
    @Test
    public void server2Test() {
        RpcServer rs1 = new RpcServer("0.0.0.0:8765");
        rs1.start(false);

        RpcServer rs2 = new RpcServer("0.0.0.0:8766");
        rs2.start(true);
    }

    @Test
    public void clientTest() throws InterruptedException {

        RpcClientManager rpcClientManager = new RpcClientManager();

        ArrayList<RpcClientConfig> list = new ArrayList<>();
        list.add(RpcClientConfig.builder()
                .appName("A")
                .host("0.0.0.0")
                .port(8765)
                .build());
        list.add(RpcClientConfig.builder()
                .appName("A")
                .host("0.0.0.0")
                .port(8766)
                .build());
        rpcClientManager.start(list);

        // 等待 2 秒后，再获取
        // 后续在来解决这里获取需要先手动等待的问题
        TimeUnit.SECONDS.sleep(2);

        // 获得一个服务的 连接
        RpcClient rpcClient = rpcClientManager.chooseClient("A");
        rpcClient.sendRequest();

        // 发送完成之后，等待几秒，等待客户端的响应
        TimeUnit.SECONDS.sleep(5);

        // 关闭所有的连接
        rpcClientManager.stop();
    }
}
