package cn.mrcode.study.note_ztc_netty.rapid.rpc.config.provider;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.server.RpcServer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 服务端启动配置类
 *
 * @author mrcode
 * @date 2022/9/28 21:51
 */
@Slf4j
public class RpcServerConfig {
    private final String host = "127.0.0.1";
    private int port;
    private List<ProviderConfig> providerConfigs;

    private RpcServer rpcServer = null;

    public RpcServerConfig(List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
    }

    public void exporter() {
        try {
            if (rpcServer == null) {
                rpcServer = new RpcServer(host + ":" + port);
            }
        } catch (Exception e) {
            log.error("服务启动失败");
        }

        for (ProviderConfig providerConfig : providerConfigs) {
            rpcServer.registerProcessor(providerConfig);
        }
    }
}
