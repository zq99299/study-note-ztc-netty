package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author mrcode
 * @date 2022/9/18 19:21
 */
class RpcConnectManagerTest {
    public static void main(String[] args) {
        RpcConnectManager rcm = RpcConnectManager.getInstance();
        rcm.connect("localhost:8086");
    }
}