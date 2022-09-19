package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

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
}
