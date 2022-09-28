package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

/**
 * rpc 异步代理
 *
 * @author mrcode
 * @date 2022/9/26 22:37
 */
public interface RpcAsyncProxy {
    RpcFuture call(String functionName, Object... args);
}
