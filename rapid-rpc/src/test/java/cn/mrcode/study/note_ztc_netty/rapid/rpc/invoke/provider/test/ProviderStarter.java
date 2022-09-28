package cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.provider.test;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.config.provider.ProviderConfig;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.config.provider.RpcServerConfig;

import java.util.Arrays;
import java.util.List;

/**
 * 服务端测试类
 *
 * @author mrcode
 * @date 2022/9/28 22:02
 */
public class ProviderStarter {
    public static void main(String[] args) {
        // 服务端启动
        new Thread(() -> {
            // 对每一个具体的服务提供配置类
            ProviderConfig providerConfig = new ProviderConfig();
            providerConfig.setInterfaceClass("cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.HelloService");
            providerConfig.setRef(new HelloServiceImpl());

            // 把所有的 providerConfig 添加到集合中
            List<ProviderConfig> providerConfigs = Arrays.asList(providerConfig);
            RpcServerConfig rpcServerConfig = new RpcServerConfig(providerConfigs);
            rpcServerConfig.setPort(8675);
            rpcServerConfig.exporter();
        }).start();
    }
}
